package com.lenta.bp12.managers.base

import com.lenta.bp12.managers.interfaces.ITaskManager
import com.lenta.bp12.model.Taskable
import com.lenta.bp12.model.pojo.*
import com.lenta.bp12.model.pojo.extentions.*
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue
import kotlin.math.floor

abstract class BaseTaskManager<T : Taskable> : ITaskManager<T> {

    abstract val database: IDatabaseRepository

    override var ean: String = ""

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

    /** Метод ищет корзины или создает их в зависимости от того что вернет getBasket() */
    private suspend fun getOrCreateSuitableBasket(
            task: T,
            good: Good,
            provider: ProviderInfo
    ): Basket {
        return withContext(Dispatchers.IO) {
            val basketVolume = database.getBasketVolume() ?: error(NULL_BASKET_VOLUME)
            val basketList = task.baskets

            //Найдем корзину в списке корзин задания
            getBasket(
                    providerCode = provider.code.orEmpty(),
                    goodToAdd = good,
                    isSaveToTask = true
            ) //Функция возвращает либо корзину с подходящими параметрами и достаточным объемом или возвращает null
                    .orIfNull {
                        //Если корзина не найдена - создадим ее
                        val index = basketList.lastOrNull()?.index?.plus(1) ?: INDEX_OF_FIRST_BASKET
                        val taskType = task.type
                        Basket(
                                index = index,
                                volume = basketVolume,
                                section = good.section
                                        .takeIf { taskType?.isDivBySection == true },
                                provider = provider
                                        .takeIf { taskType?.isDivByProvider == true },
                                control = good.control
                                        .takeIf { taskType?.isDivByGis == true },
                                goodType = good.type
                                        .takeIf { taskType?.isDivByGoodType == true },
                                markTypeGroup = good.markTypeGroup
                                        .takeIf { taskType?.isDivByMark == true },
                                purchaseGroup = good.purchaseGroup
                                        .takeIf { taskType?.isDivByPurchaseGroup == true },
                                mprGroup = good.mprGroup.toString()
                                        .takeIf { taskType?.isDivByMinimalPrice == true }
                                        ?.padStart(2, '0')
                        ).also {
                            it.maxRetailPrice = good.maxRetailPrice
                            addBasket(it)
                            Logg.e { it.toString() }
                        }
                    }
        }
    }

    /** Метод ищет корзины в списке корзин задания,
     * и проверяет подходят ли параметры (divs), закрыта она или нет, и есть ли свободный объём
     */
    override suspend fun getBasket(
            providerCode: String,
            goodToAdd: Good,
            isSaveToTask: Boolean
    ): Basket? {
        return currentTask.value?.let { task ->
            val allBaskets = task.baskets.toMutableList()
            currentGood.value?.let { good ->
                task.type?.run {
                    allBaskets.find { basket ->
                        //Если в задании есть деление по параметру (task.type), то сравниваем,
                        //Если нет — то просто пропускаем как подходящий для корзины
                        val divByMark = (basket.markTypeGroup == good.markTypeGroup)
                                .takeIf { isDivByMark }
                                .orIfNull { true }
                        val divByMrc = isSameMrcGroup(basket, goodToAdd, isSaveToTask)
                                .takeIf { isDivByMinimalPrice }
                                .orIfNull { true }
                        val divBySection = (basket.section == good.section)
                                .takeIf { isDivBySection }
                                .orIfNull { true }
                        val divByType = (basket.goodType == good.type)
                                .takeIf { isDivByGoodType }
                                .orIfNull { true }
                        val divByProviders = (basket.provider?.code == providerCode)
                                .takeIf { isDivByProvider }
                                .orIfNull { true }
                        val divByPurchaseGroup = (basket.purchaseGroup == good.purchaseGroup)
                                .takeIf { isDivByPurchaseGroup }
                                .orIfNull { true }
                        val divByControl = (basket.control == good.control)
                                .takeIf { isDivByGis }
                                .orIfNull { true }
                        val divs = divByMark && divByMrc && divBySection && divByType &&
                                divByProviders && divByControl && divByPurchaseGroup

                        isLastBasketMatches(basket, good, divs)
                    }?.also {
                        updateCurrentBasket(it)
                    }
                }
            }
        }
    }

    /**
     * Метод добавляет пустую позицию, используется при добавлении марки или партии
     */
    private fun addEmptyPosition(good: Good, provider: ProviderInfo, basket: Basket) {
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
                }.orIfNull {
                    leftToDel = ZERO_QUANTITY
                    Unit
                }
                updateCurrentBasket(it)
            }.orIfNull {
                leftToDel = ZERO_QUANTITY
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
    private fun isSameMrcGroup(basket: Basket, goodToAdd: Good, saveToTask: Boolean): Boolean {
        val sameGood = basket.goods.keys.firstOrNull { it.material == goodToAdd.material }
        return sameGood?.run {
            val isSameMaxRetailPrice = maxRetailPrice == goodToAdd.maxRetailPrice
            if (!isSameMaxRetailPrice && saveToTask) {
                goodToAdd.mprGroup++
            }
            isSameMaxRetailPrice
        } ?: true
    }

    private fun isLastBasketMatches(basket: Basket, good: Good, divs: Boolean): Boolean {
        return divs && isBasketNotClosedAndHasEnoughVolume(basket, good)
    }

    private fun isBasketNotClosedAndHasEnoughVolume(basket: Basket, good: Good): Boolean =
            !basket.isLocked && isBasketHasEnoughVolume(basket, good)

    private fun isBasketHasEnoughVolume(basket: Basket, good: Good): Boolean {
        return basket.freeVolume > good.getVolumeCorrespondingToUom()
    }

    private suspend fun addGoodToBaskets(task: T, good: Good, provider: ProviderInfo, part: Part?, count: Double) {
        // Переменная которая служит счётчиком - сколько товаров надо добавить
        var leftToAdd = count
        // Пока все товары не добавлены крутимся в цикле
        while (leftToAdd > 0) {
            // Найдем корзину в которой достаточно места для нового товара, или создадим ее
            val suitableBasket = getOrCreateSuitableBasket(task, good, provider)

            //Максимальное количество этого товара, которе может влезть в эту корзину, учитывая оставшийся объем в ней
            val maxQuantity = floor(suitableBasket.freeVolume.div(good.getVolumeCorrespondingToUom()))
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

    /** Добавляет товар в корзину один раз без цикла, и при этом добавляет в товар марку */
    override suspend fun addGoodToBasketWithMark(good: Good, mark: Mark, provider: ProviderInfo) {
        currentTask.value?.let { taskValue ->
            val suitableBasket = getOrCreateSuitableBasket(taskValue, good, provider)
            // Добавим марке номер корзины
            mark.basketNumber = suitableBasket.index
            // Продублируем марку в позиции (просто надо)
            addEmptyPosition(good, provider, suitableBasket)
            // Добавим товар в корзину
            suitableBasket.addGood(good, 1.0)
            // Добавим товар в задание
            saveGoodInTask(good)
            // Обновим товар в менеджере
            updateCurrentGood(good)

            if (isBasketsNeedsToBeClosed) {
                suitableBasket.isLocked = true
            }
        }
    }

    override suspend fun addGoodToBasketWithMarks(good: Good, marks: List<Mark>, provider: ProviderInfo) {
        currentTask.value?.let { taskValue ->
            marks.forEach { mark ->
                val suitableBasket = getOrCreateSuitableBasket(taskValue, good, provider)

                // Добавим марке номер корзины
                mark.basketNumber = suitableBasket.index

                // Продублируем марку в позиции (просто надо)
                addEmptyPosition(good, provider, suitableBasket)
                // Добавим товар в корзину
                suitableBasket.addGood(good, 1.0)
                // Добавим товар в задание
                saveGoodInTask(good)
                // Обновим товар в менеджере
                updateCurrentGood(good)

                if (isBasketsNeedsToBeClosed) {
                    suitableBasket.markedForLock = true
                }
            }

            taskValue.baskets.filter { it.markedForLock }.forEach {
                it.isLocked = true
                it.markedForLock = false
            }
        }
    }

    fun updateCurrentTask(task: T?) {
        currentTask.postValue(task)
    }

    override fun updateCurrentGood(good: Good?) {
        currentGood.postValue(good)
    }

    override fun updateCurrentBasket(basket: Basket?) {
        currentBasket.postValue(basket)
    }

    override fun clearCurrentGood() {
        currentGood.value = null
    }

    override fun saveGoodInTask(good: Good) {
        currentTask.value?.let { task ->
            task.goods.find {
                (it.material == good.material) && (it.maxRetailPrice == good.maxRetailPrice)
            }?.let { good ->
                task.goods.remove(good)
            }
            task.goods.add(0, good)
            updateCurrentTask(task)
        }
    }

    override fun addBasket(basket: Basket) {
        currentTask.value?.let { task ->
            task.baskets.add(basket)
            updateCurrentTask(task)
        }

        updateCurrentBasket(basket)
    }

    override suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean {
        return database.isGoodCanBeAdded(goodInfo, currentTask.value?.type?.code.orEmpty())
    }

    override fun findGoodByEan(ean: String): Good? {
        return currentTask.value?.let { task ->
            task.goods.find { good ->
                good.isGoodHasSameEan(ean)
            }?.also { found ->
                found.ean = ean
                updateCurrentTask(task)
            }
        }
    }

    override fun findGoodByEanAndMRC(ean: String, mrc: String): Good? {
        return if (mrc.isEmpty()) {
            findGoodByEan(ean)
        } else {
            currentTask.value?.let { task ->
                task.goods.find { good ->
                    good.isGoodHasSameEan(ean) && good.isGoodHasSameMaxRetailPrice(mrc)
                }
            }
        }
    }

    override fun findGoodByMaterial(material: String): Good? {
        return currentTask.value?.goods?.find { it.material == material }
    }

    override fun removeMarksFromGoods(mappedMarks: List<Mark>) {
        currentTask.value?.let { task ->
            task.goods.find {
                it.marks.isAnyAlreadyIn(mappedMarks)
            }?.let { good ->
                task.baskets.forEach {
                    if (it.goods.containsKey(good)) {
                        it.deleteGoodByMarks(good)
                    }
                }
                good.removeMarks(mappedMarks)
            }
            task.removeEmptyBaskets()
            task.removeEmptyGoods()

            updateCurrentTask(task)
        }
    }

    override fun removeBaskets(basketList: MutableList<Basket>) {
        currentTask.value?.let { task ->
            task.removeBaskets(basketList)
            updateCurrentTask(task)
        }
    }

    override fun clearEan() {
        this.ean = ""
    }

    override fun deleteGood(good: Good) {
        currentTask.value?.run {
            goods.remove(good)
            baskets.forEach { it.deleteGood(good) }
            removeEmptyGoods()
            removeEmptyBaskets()
            updateCurrentTask(this)
        }
    }

    companion object {
        private const val NULL_BASKET_VOLUME = "Объем корзины отсутствует"
        private const val INDEX_OF_FIRST_BASKET = 1
    }

}