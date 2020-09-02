package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.Part
import com.lenta.bp12.model.pojo.Position
import com.lenta.bp12.model.pojo.create_task.GoodCreate
import com.lenta.bp12.model.pojo.create_task.TaskCreate
import com.lenta.bp12.model.pojo.extentions.*
import com.lenta.bp12.platform.extention.isAlcohol
import com.lenta.bp12.platform.extention.isCommon
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.SendTaskDataParams
import com.lenta.bp12.request.pojo.*
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.toSapBooleanString
import com.lenta.shared.utilities.getStringFromDate
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.floor

class CreateTaskManager @Inject constructor(
        private val database: IDatabaseRepository,
        private val generalTaskManager: IGeneralTaskManager
) : ICreateTaskManager {

    override var searchNumber = ""

    override var isSearchFromList = false

    override var isWasAddedProvider = false

    override var isWholesaleTaskType: Boolean = false
    override var isBasketsNeedsToBeClosed: Boolean = false

    override val currentTask = MutableLiveData<TaskCreate>()

    override val currentGood = MutableLiveData<GoodCreate>()

    override val currentBasket = MutableLiveData<Basket>()

    /** Метод добавляет обычные в товары в корзину */
    override suspend fun addGoodToBasket(
            good: GoodCreate,
            part: Part?,
            provider: ProviderInfo,
            count: Double
    ) {
        currentTask.value?.let { taskValue ->
            // Переменная которая служит счетчиком - сколько товаров надо добавить
            var leftToAdd = count
            // Пока все товары не добавлены крутимся в цикле
            while (leftToAdd > 0) {
                // Найдем корзину в которой достаточно места для нового товара, или создадим ее
                val suitableBasket = getOrCreateSuitableBasket(taskValue, good, provider)

                //Максимальное количество этого товара, которе может влезть в эту корзину, учитывая оставшийся объем в ней
                val maxQuantity = floor(suitableBasket.freeVolume.div(good.volume))
                //Если макс количество больше чем нужно добавить
                val quantity = if (maxQuantity >= leftToAdd) {
                    leftToAdd // то добавляем все что осталось добавить
                } else {
                    maxQuantity // или только то количество что влезет
                }

                //Если передаем партию, то
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
            taskValue.baskets.filter { it.markedForLock }.forEach {
                it.isLocked = true
                it.markedForLock = false
            }
        }
    }

    /** Добавляет товар в корзину один раз без цикла, и при этом добавляет в товар марку */
    override suspend fun addGoodToBasketWithMark(good: GoodCreate, mark: Mark, provider: ProviderInfo) {
        currentTask.value?.let { taskValue ->
            val suitableBasket = getOrCreateSuitableBasket(taskValue, good, provider)

            // Добавим марке номер корзины
            mark.basketNumber = suitableBasket.index
            // Положим в товар
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

            Logg.e { "BASKET GOODS: ${suitableBasket.goods}" }
        }
    }

    private fun addEmptyPosition(good: GoodCreate, provider: ProviderInfo, basket: Basket) {
        val position = Position(
                quantity = 0.0,
                provider = provider
        )
        position.basketNumber = basket.index
        Logg.d { "--> add position = $position" }
        good.addPosition(position)
    }

    /** Метод ищет корзины или создает их в зависимости от того что вернет getBasket() */
    override suspend fun getOrCreateSuitableBasket(task: TaskCreate, good: GoodCreate, provider: ProviderInfo): Basket {
        return withContext(Dispatchers.IO) {
            val basketVolume = database.getBasketVolume() ?: error(NULL_BASKET_VOLUME)
            val basketList = task.baskets

            //Найдем корзину в списке корзин задания
            getBasket(provider.code.orEmpty()) //Функция возвращает либо корзину с подходящими параметрами и достаточным объемом или возвращает null
                    .orIfNull {
                        //Если корзина не найдена - создадим ее
                        val index = basketList.lastOrNull()?.index?.plus(1) ?: INDEX_OF_FIRST_BASKET
                        Basket(
                                index = index,
                                section = good.section,
                                volume = basketVolume,
                                provider = provider,
                                control = good.control,
                                goodType = good.type,
                                markTypeGroup = good.markTypeGroup
                        ).also {
                            addBasket(it)
                        }
                    }
        }
    }

    /** Метод ищет корзины в списке корзин задания,
     * и проверяет подходят ли параметры, закрыта она или нет, и есть ли свободный объём
     * divByMark проверяет нужно ли деление корзины по маркам, если нужно то сравнивает марку корзины и товара
     * если нельзя то просто передает true */
    override fun getBasket(providerCode: String): Basket? {
        return currentTask.value?.let { task ->
            currentGood.value?.let { good ->
                task.baskets.lastOrNull { basket ->
                    val divByMark = if (task.type.isDivByMark) basket.markTypeGroup == good.markTypeGroup else true
                    isLastBasketMatches(basket, good, providerCode, divByMark)
                }
            }
        }
    }

    private fun isLastBasketMatches(
            basket: Basket,
            good: GoodCreate,
            providerCode: String,
            divByMark: Boolean
    ): Boolean {
        return divByMark && basket.section == good.section &&
                basket.goodType == good.type &&
                basket.control == good.control &&
                basket.provider?.code == providerCode &&
                !basket.isLocked &&
                isBasketHasEnoughVolume(basket, good)
    }

    private fun isBasketHasEnoughVolume(basket: Basket, good: GoodCreate): Boolean {
        return basket.freeVolume > good.volume
    }

    override fun updateCurrentTask(task: TaskCreate?) {
        currentTask.postValue(task)
    }

    override fun updateCurrentGood(good: GoodCreate?) {
        currentGood.postValue(good)
    }

    override fun updateCurrentBasket(basket: Basket?) {
        currentBasket.postValue(basket)
    }

    override fun clearCurrentGood() {
        currentGood.value = null
    }

    override fun saveGoodInTask(good: GoodCreate) {
        currentTask.value?.let { task ->
            task.goods.find { it.material == good.material }?.let { good ->
                task.goods.remove(good)
            }

            task.goods.add(good)
            updateCurrentTask(task)
        }
    }

    override fun findGoodByEan(ean: String): GoodCreate? {
        return currentTask.value?.let { task ->
            task.goods.find { good ->
                good.ean == ean || good.eans.contains(ean)
            }?.also { found ->
                found.ean = ean
                updateCurrentTask(task)
            }
        }
    }

    override fun findGoodByMaterial(material: String): GoodCreate? {
        return currentTask.value?.goods?.find { it.material == material }
    }

    override suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean {
        return database.isGoodCanBeAdded(goodInfo, currentTask.value?.type?.code.orEmpty())
    }

    override fun addBasket(basket: Basket) {
        currentTask.value?.let { task ->
            task.baskets.add(basket)
            updateCurrentTask(task)
        }

        updateCurrentBasket(basket)
    }

    override fun removeGoodByMaterials(materialList: List<String>) {
        currentTask.value?.let { task ->
            task.removeGoodByMaterials(materialList)
            updateCurrentTask(task)
        }
    }

    override fun removeBaskets(basketList: MutableList<Basket>) {
        currentTask.value?.let { task ->
            task.removeBaskets(basketList)
            updateCurrentTask(task)
        }
    }

    override fun addProviderInCurrentGood(providerInfo: ProviderInfo) {
        currentGood.value?.let { good ->
            good.providers.add(0, providerInfo)
            isWasAddedProvider = true

            updateCurrentGood(good)
        }
    }

    override fun prepareSendTaskDataParams(deviceIp: String, tkNumber: String, userNumber: String) {
        currentTask.value?.let { task ->
            //IT_TASK_POS
            val positions = mutableListOf<PositionInfo>()
            val marks = mutableListOf<ExciseMarkInfo>()
            val parts = mutableListOf<PartInfo>()
            val baskets = mutableListOf<CreateTaskBasketInfo>()
            val basketPositions = mutableListOf<BasketPositionInfo>()

            task.baskets.forEach { basket ->
                val basketNumber = "${basket.index}"

                baskets.add(
                        CreateTaskBasketInfo( //IT_TASK_BASKET
                                basketNumber = basketNumber,
                                isCommon = basket.control?.isCommon().toSapBooleanString(),
                                isAlcohol = basket.control?.isAlcohol().toSapBooleanString(),
                                providerCode = basket.provider?.code,
                                goodType = basket.goodType,
                                section = basket.section
                        )
                )

                basket.getGoodList().mapTo(basketPositions) { good ->
                    BasketPositionInfo( //IT_TASK_BASKET_POS
                            material = good.material,
                            basketNumber = basketNumber,
                            quantity = basket.getQuantityOfGood(good).dropZeros()
                    )
                }
            }

            task.goods.forEach { good ->
                good.positions.mapTo(positions) { position ->
                    val quantity = if (position.quantity > 0.0) position.quantity else good.getTotalQuantity()
                    PositionInfo( //IT_TASK_POS
                            material = good.material,
                            providerCode = position.provider.code.orEmpty(),
                            providerName = position.provider.name.orEmpty(),
                            factQuantity = quantity.dropZeros(),
                            isCounted = true.toSapBooleanString(),
                            innerQuantity = good.innerQuantity.dropZeros(),
                            unitsCode = good.commonUnits.code,
                            maxRetailPrice = good.maxRetailPrice
                    )
                }

                good.marks.mapTo(marks) { mark ->
                    ExciseMarkInfo( //IT_TASK_MARK
                            material = good.material,
                            number = mark.number,
                            boxNumber = mark.boxNumber,
                            isBadMark = mark.isBadMark.toSapBooleanString(),
                            providerCode = mark.providerCode,
                            basketNumber = mark.basketNumber.toString(),
                            maxRetailPrice = mark.maxRetailPrice,
                            packNumber = mark.packNumber
                    )
                }

                good.parts.mapTo(parts) { part ->
                    PartInfo( //IT_TASK_PARTS
                            material = good.material,
                            producerCode = part.producerCode,
                            productionDate = getStringFromDate(part.date, Constants.DATE_FORMAT_yyyyMMdd),
                            quantity = part.quantity.dropZeros(),
                            partNumber = part.number,
                            providerCode = part.providerCode,
                            basketNumber = part.basketNumber.toString()
                    )
                }
            }

            generalTaskManager.setSendTaskDataParams(
                    SendTaskDataParams(
                            deviceIp = deviceIp,
                            userNumber = userNumber,
                            taskName = task.name,
                            taskType = task.type.code,
                            tkNumber = tkNumber,
                            storage = task.storage,
                            reasonCode = task.reason.code,
                            isNotFinish = false.toSapBooleanString(),
                            positions = positions,
                            exciseMarks = marks,
                            parts = parts,
                            baskets = baskets,
                            basketPositions = basketPositions
                    )
            )
        }
    }

    override fun clearSearchFromListParams() {
        isSearchFromList = false
        searchNumber = ""
    }

    companion object {
        private const val NULL_BASKET_VOLUME = "Объем корзины отсутствует"
        private const val INDEX_OF_FIRST_BASKET = 1
    }

}


interface ICreateTaskManager : ITaskManager {

    val currentGood: MutableLiveData<GoodCreate>
    val currentTask: MutableLiveData<TaskCreate>
    var isWasAddedProvider: Boolean

    suspend fun addGoodToBasket(good: GoodCreate, part: Part? = null, provider: ProviderInfo, count: Double)
    suspend fun addGoodToBasketWithMark(good: GoodCreate, mark: Mark, provider: ProviderInfo)
    suspend fun getOrCreateSuitableBasket(task: TaskCreate, good: GoodCreate, provider: ProviderInfo): Basket?

    fun updateCurrentTask(task: TaskCreate?)
    fun updateCurrentGood(good: GoodCreate?)

    fun findGoodByEan(ean: String): GoodCreate?
    fun findGoodByMaterial(material: String): GoodCreate?

    fun removeGoodByMaterials(materialList: List<String>)
    fun addProviderInCurrentGood(providerInfo: ProviderInfo)
    fun saveGoodInTask(good: GoodCreate)
}