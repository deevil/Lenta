package com.lenta.bp12.managers.base

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.managers.interfaces.ITaskManager
import com.lenta.bp12.model.Taskable
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Part
import com.lenta.bp12.model.pojo.Position
import com.lenta.bp12.model.pojo.extentions.addGood
import com.lenta.bp12.model.pojo.extentions.addPart
import com.lenta.bp12.model.pojo.extentions.addPosition
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue
import kotlin.math.floor

abstract class BaseTaskManager<T : Taskable> : ITaskManager {

    abstract val database: IDatabaseRepository
    abstract val currentTask: MutableLiveData<T>

    /** Метод добавляет обычные в товары в корзину */
    override suspend fun addOrDeleteGoodToBasket(good: Good, part: Part?, provider: ProviderInfo, count: Double) {
        currentTask.value?.let { taskValue ->
            // Переменная которая служит счетчиком - сколько товаров надо добавить или удалить
            if (count < 0) {
                deleteGoodFromBaskets(taskValue, good, count)
            } else {
                addGoodToBaskets(taskValue, good, provider, part, count)
            }
        }
    }

    /** Метод ищет корзины в списке корзин задания,
     * и проверяет подходят ли параметры (divs), закрыта она или нет, и есть ли свободный объём
     */
    override suspend fun getBasket(providerCode: String, goodToAdd: Good): Basket? {
        return currentTask.value?.let { task ->
            val allBaskets = ArrayList(task.baskets)
            currentGood.value?.let { good ->
                task.type?.run {
                    allBaskets.find { basket ->
                        //Если в задании есть деление по параметру (task.type), то сравниваем,
                        //Если нет — то просто пропускаем как подходящий для корзины
                        val divByMark = if (isDivByMark) basket.markTypeGroup == good.markTypeGroup else true
                        val divByMrc = if (isDivByMinimalPrice) isSameMrcGroup(basket, goodToAdd) else true
                        val divBySection = if (isDivBySection) basket.section == good.section else true
                        val divByType = if (isDivByGoodType) basket.goodType == good.type else true
                        val divByProviders = if (isDivByProvider) basket.provider?.code == providerCode else true
                        val divByControl = basket.control == good.control
                        val divs = divByMark && divByMrc && divBySection && divByType && divByProviders && divByControl
                        isLastBasketMatches(basket, good, divs)
                    }?.also {
                        updateCurrentBasket(it)
                    }
                }
            }
        }
    }

    /** Метод ищет корзины или создает их в зависимости от того что вернет getBasket() */
    suspend fun getOrCreateSuitableBasket(
            task: T,
            good: Good,
            provider: ProviderInfo
    ): Basket {
        return withContext(Dispatchers.IO) {
            val basketVolume = database.getBasketVolume() ?: error(NULL_BASKET_VOLUME)
            val basketList = task.baskets

            //Найдем корзину в списке корзин задания
            getBasket(provider.code.orEmpty(), good) //Функция возвращает либо корзину с подходящими параметрами и достаточным объемом или возвращает null
                    .orIfNull {
                        //Если корзина не найдена - создадим ее
                        val index = basketList.lastOrNull()?.index?.plus(1) ?: INDEX_OF_FIRST_BASKET
                        Basket(
                                index = index,
                                section = good.section.takeIf { task.type?.isDivBySection == true },
                                volume = basketVolume,
                                provider = provider.takeIf { task.type?.isDivByProvider == true },
                                control = good.control,
                                goodType = good.type.takeIf { task.type?.isDivByGoodType == true },
                                markTypeGroup = good.markTypeGroup,
                                purchaseGroup = good.purchaseGroup.takeIf { task.type?.isDivByPurchaseGroup == true }
                        ).also {
                            it.maxRetailPrice = good.maxRetailPrice
                            addBasket(it)
                        }
                    }
        }
    }

    /**
     * Метод добавляет пустую позицию, используется при добавлении марки или партии
     */
    protected fun addEmptyPosition(good: Good, provider: ProviderInfo, basket: Basket) {
        good.isCounted = true
        val position = Position(
                quantity = ZERO_QUANTITY,
                provider = provider
        )
        position.basketNumber = basket.index
        Logg.d { "--> add position = $position" }
        good.addPosition(position)
    }

    protected open fun deleteGoodFromBaskets(task: Taskable, good: Good, count: Double) {
        var leftToDel = count.absoluteValue
        val baskets = task.getBasketsByGood(good).toMutableList()
        while (leftToDel > 0) {
            val lastBasket = baskets.lastOrNull()
            lastBasket?.let {
                val oldQuantity = lastBasket.goods[good]
                oldQuantity?.let {
                    val newQuantity = oldQuantity.minus(leftToDel)
                    if (newQuantity <= 0) {
                        leftToDel = newQuantity.absoluteValue
                        lastBasket.goods.remove(good)
                        baskets.remove(lastBasket)
                    } else {
                        lastBasket.goods[good] = newQuantity
                        leftToDel = ZERO_QUANTITY
                    }
                }
                updateCurrentBasket(it)
            }
        }
        task.removeEmptyBaskets()
        task.removeEmptyGoods()
    }

    /** Метод проверяет группу мрц товара
     * одинаковые товары с разным мрц в разные корзины,
     * разные товары с одинаковым мрц в одну корзину
     * разные товары с разными мрц в одну корзину
     * */
    private fun isSameMrcGroup(basket: Basket, goodToAdd: Good): Boolean {
        val sameGood = basket.goods.keys.firstOrNull { it.material == goodToAdd.material }
        return sameGood?.run {
            maxRetailPrice == goodToAdd.maxRetailPrice
        } ?: true
    }

    private fun isLastBasketMatches(basket: Basket, good: Good, divs: Boolean): Boolean {
        return divs && isBasketNotClosedAndHasEnoughVolume(basket, good)
    }

    private fun isBasketNotClosedAndHasEnoughVolume(basket: Basket, good: Good): Boolean =
            !basket.isLocked && isBasketHasEnoughVolume(basket, good)

    private fun isBasketHasEnoughVolume(basket: Basket, good: Good): Boolean {
        return basket.freeVolume > good.volume
    }

    private suspend fun addGoodToBaskets(task: T, good: Good, provider: ProviderInfo, part: Part?, count: Double) {
        // Переменная которая служит счётчиком - сколько товаров надо добавить
        var leftToAdd = count
        // Пока все товары не добавлены крутимся в цикле
        while (leftToAdd > 0) {
            // Найдем корзину в которой достаточно места для нового товара, или создадим ее
            val suitableBasket = getOrCreateSuitableBasket(task, good, provider)

            //Максимальное количество этого товара, которе может влезть в эту корзину, учитывая оставшийся объем в ней
            val maxQuantity = floor(suitableBasket.freeVolume.div(good.volume))
            //Если макс количество больше чем нужно добавить
            val quantity = if (maxQuantity >= leftToAdd) {
                leftToAdd // то добавляем все что осталось добавить
            } else {
                maxQuantity // или только то количество что влезет
            }

            if (part != null) {
                // Скопируем партию потому что сверху приходит одна, для каждой корзины будет своя партия
                val newPart = part.copy()
                // Укажем партии количество в корзине и номер корзины
                newPart.quantity = quantity
                newPart.basketNumber = suitableBasket.index
                // Добавим партию в товар
                good.addPart(newPart)
                // Добавим пустую позицию товара (просто надо)
                addEmptyPosition(good, provider, suitableBasket)
                // Добавим товар в корзину
            }
            // Добавим товар в корзину
            suitableBasket.addGood(good, quantity)
            // Уменьшим количество товара которое осталось добавить
            leftToAdd -= quantity

            //Обновим товар в задании
            updateCurrentGood(good)

            // Если нажата кнопка закрыть корзину то пометим все корзины для закрытия
            if (isBasketsNeedsToBeClosed) {
                suitableBasket.markedForLock = true
            }
        }

        // После того как распределим все товары по корзинам, закроем отмеченные для закрытия
        task.baskets.filter { it.markedForLock }.forEach {
            it.isLocked = true
            it.markedForLock = false
        }
    }

    companion object {
        private const val NULL_BASKET_VOLUME = "Объем корзины отсутствует"
        private const val INDEX_OF_FIRST_BASKET = 1
    }

}