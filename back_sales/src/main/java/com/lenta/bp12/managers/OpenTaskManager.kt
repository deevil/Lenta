package com.lenta.bp12.managers

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.managers.interfaces.IGeneralTaskManager
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.pojo.*
import com.lenta.bp12.model.pojo.extentions.*
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.platform.DEFAULT_QUANTITY
import com.lenta.bp12.platform.ZERO_VOLUME
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.isAlcohol
import com.lenta.bp12.platform.extention.isCommon
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.SendTaskDataParams
import com.lenta.bp12.request.TaskContentResult
import com.lenta.bp12.request.pojo.*
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.bp12.request.pojo.taskContentNetRequest.toMrcList
import com.lenta.shared.models.core.getInnerUnits
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.extentions.toSapBooleanString
import com.lenta.shared.utilities.getStringFromDate
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.floor

class OpenTaskManager @Inject constructor(
        private val database: IDatabaseRepository,
        private val generalTaskManager: IGeneralTaskManager
) : IOpenTaskManager {

    override var isNeedLoadTaskListByParams: Boolean = false

    override var isWholesaleTaskType: Boolean = false

    override var isBasketsNeedsToBeClosed: Boolean = false

    override var searchParams: TaskSearchParams? = null

    override val tasks = MutableLiveData<List<TaskOpen>>(emptyList())

    override val foundTasks = MutableLiveData<List<TaskOpen>>(emptyList())

    override val currentTask = MutableLiveData<TaskOpen>()

    override val currentGood = MutableLiveData<Good>()

    override val currentBasket = MutableLiveData<Basket>()

    private var startStateHashOfCurrentTask = -1

    /** Метод добавляет обычные в товары в корзину */
    override suspend fun addGoodToBasket(good: Good, part: Part?, provider: ProviderInfo, count: Double) {
        currentTask.value?.let { taskValue ->
            // Переменная которая служит счётчиком - сколько товаров надо добавить
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
        }

        // После того как распределим все товары по корзинам, закроем отмеченные для закрытия
        currentTask.value?.let { taskValue ->
            taskValue.baskets.filter { it.markedForLock }.forEach {
                it.isLocked = true
                it.markedForLock = false
            }
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

    /**
     * Метод добавляет пустую позицию, используется при добавлении марки или партии
     */
    private fun addEmptyPosition(good: Good, provider: ProviderInfo, basket: Basket) {
        good.isCounted = true
        val position = Position(
                quantity = DEFAULT_QUANTITY,
                provider = provider
        )
        position.basketNumber = basket.index
        Logg.d { "--> add position = $position" }
        good.addPosition(position)
    }

    /** Метод ищет корзины или создает их в зависимости от того что вернет getBasket() */
    override suspend fun getOrCreateSuitableBasket(
            task: TaskOpen,
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
                                section = good.section.takeIf{ task.type?.isDivBySection == true},
                                volume = basketVolume,
                                provider = provider.takeIf{ task.type?.isDivByProvider == true},
                                control = good.control,
                                goodType = good.type.takeIf{ task.type?.isDivByGoodType == true},
                                markTypeGroup = good.markTypeGroup
                        ).also {
                            it.maxRetailPrice = good.maxRetailPrice
                            addBasket(it)
                        }
                    }
        }
    }

    /** Метод ищет корзины в списке корзин задания,
     * и проверяет подходят ли параметры, закрыта она или нет, и есть ли свободный объём */
    override fun getBasket(providerCode: String, goodToAdd: Good): Basket? {
        return currentTask.value?.let { task ->
            currentGood.value?.let { good ->
                task.type?.let { type ->
                    with(type){
                        task.baskets.lastOrNull { basket ->
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
    }

    /** Метод проверяет группу мрц товара
     * одинаковые товары с разным мрц в разные корзины,
     * разные товары с одинаковым мрц в одну корзину
     * разные товары с разными мрц в одну корзину
     * */
    private fun isSameMrcGroup(basket: Basket, goodToAdd: Good): Boolean {
        val sameGood = basket.goods.keys.firstOrNull { it.material == goodToAdd.material }
        return sameGood?.let {
            it.maxRetailPrice == goodToAdd.maxRetailPrice
        } ?: true
    }

    private fun isLastBasketMatches(basket: Basket, good: Good, divs: Boolean): Boolean {
        return divs && isBasketNotClosedAndHasEnoughVolume(basket, good)
    }

    private fun isBasketNotClosedAndHasEnoughVolume(basket: Basket, good: Good): Boolean =
            !basket.isLocked && isBasketHasEnoughVolume(basket, good)

    override fun updateTasks(taskList: List<TaskOpen>?) {
        tasks.value = taskList ?: emptyList()
    }

    private fun isBasketHasEnoughVolume(basket: Basket, good: Good): Boolean {
        return basket.freeVolume > good.volume
    }

    override fun updateFoundTasks(taskList: List<TaskOpen>?) {
        foundTasks.value = taskList ?: emptyList()
    }

    override fun updateCurrentTask(task: TaskOpen?) {
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
            task.goods.find { it.material == good.material }?.let { good ->
                task.goods.remove(good)
            }

            task.goods.add(0, good)
            updateCurrentTask(task)
        }
    }

    override suspend fun addTasks(tasksInfo: List<TaskInfo>) {
        updateTasks(getTaskListFromInfo(tasksInfo))
    }

    override suspend fun addFoundTasks(tasksInfo: List<TaskInfo>) {
        updateFoundTasks(getTaskListFromInfo(tasksInfo))
    }

    private suspend fun getTaskListFromInfo(tasksInfo: List<TaskInfo>): List<TaskOpen> {
        return tasksInfo.map { taskInfo ->
            taskInfo.convertToTaskOpen(
                    type = database.getTaskType(taskInfo.typeCode.orEmpty()),
                    reason = database.getReturnReason(taskInfo.typeCode.orEmpty(), taskInfo.reasonCode.orEmpty())
            )
        }
    }

    override suspend fun addTaskContentInCurrentTask(taskContentResult: TaskContentResult) {
        currentTask.value?.let { task ->
            task.addGoodsToTask(taskContentResult)
            task.addBasketsToTask(taskContentResult)
            task.addMrcListToTask(taskContentResult)
            updateCurrentTask(task)
        }
    }

    private fun TaskOpen.addMrcListToTask(taskContentResult: TaskContentResult) {
        taskContentResult.mrcList?.let {
            mrcList.clear()
            val newMrcList = it.toMrcList()
            mrcList.addAll(newMrcList)
        }
    }

    private suspend fun TaskOpen.addGoodsToTask(taskContentResult: TaskContentResult) {
        taskContentResult.positions?.map { positionInfo ->
            with(positionInfo) {
                database.getGoodInfoByMaterial(material.orEmpty())?.let { goodInfo ->
                    val commonUnits = database.getUnitsByCode(unitsCode.orEmpty())
                    val provider = ProviderInfo(providerCode.orEmpty(), providerName.orEmpty())
                    val markType = goodInfo.markType

                    val good = Good(
                            ean = goodInfo.ean,
                            eans = goodInfo.eans,
                            material = material.orEmpty(),
                            name = goodInfo.name,
                            section = goodInfo.section,
                            matrix = goodInfo.matrix,
                            kind = goodInfo.kind,
                            planQuantity = planQuantity?.toDoubleOrNull() ?: DEFAULT_QUANTITY,
                            factQuantity = factQuantity?.toDoubleOrNull() ?: DEFAULT_QUANTITY,
                            commonUnits = commonUnits,
                            innerUnits = getInnerUnits(commonUnits),
                            innerQuantity = innerQuantity?.toDoubleOrNull() ?: 1.0,
                            isCounted = isCounted.isSapTrue(),
                            isDeleted = isDeleted.isSapTrue(),
                            provider = provider,
                            producers = taskContentResult.producers?.filter { it.material == goodInfo.material }?.map {
                                ProducerInfo(
                                        code = it.code,
                                        name = it.name
                                )
                            }?.toMutableList() ?: mutableListOf(),
                            volume = positionInfo.volume?.toDoubleOrNull() ?: ZERO_VOLUME,
                            markType = markType,
                            markTypeGroup = database.getMarkTypeGroupByMarkType(markType),
                            maxRetailPrice = positionInfo.maxRetailPrice?.toDoubleOrNull().dropZeros(),
                            type = ""
                    )

                    factQuantity?.toDoubleOrNull()?.let { factQuantity ->
                        if (factQuantity != DEFAULT_QUANTITY) {
                            good.addPosition(Position(
                                    quantity = factQuantity,
                                    provider = provider
                            ))
                        }
                    }

                    goods.add(good)
                    Logg.d { "--> added good = $good" }
                }
            }
        }
    }

    private suspend fun TaskOpen.addBasketsToTask(taskContentResult: TaskContentResult) {
        val basketVolume = database.getBasketVolume() ?: 0.0
        val taskGoods = goods
        //Корзины с реста
        val restBaskets = taskContentResult.basketInfo
        // Таблица соотношения товаров и корзин, приходит так: [BasketPositionInfo{ХЛЕБ, КОРЗИНА 1, 32ШТ}, BasketPositionInfo{ХЛЕБ, КОРЗИНА 2, 15ШТ}]
        val restBasketsProducts = taskContentResult.basketProducts
        // Выше мы создали мапу - [ХЛЕБ - [BasketPositionInfo{ХЛЕБ, КОРЗИНА 1, 32ШТ, ""}, BasketPositionInfo{ХЛЕБ, КОРЗИНА 2, 15ШТ}]].
        val mapOfGoodsByMaterial = restBasketsProducts?.groupBy { it.material.orEmpty() }.orEmpty()

        val mappedBaskets = restBaskets?.map { restBasket ->
            val markTypeGroup = database.getMarkTypeGroupByCode(restBasket.marktypeGroup)
            Basket(
                    index = restBasket.basketNumber?.toIntOrNull()
                            ?: baskets.size + 1,
                    section = restBasket.section.orEmpty(),
                    goodType = restBasket.goodType.orEmpty(),
                    control = control,
                    provider = taskGoods.firstOrNull()?.provider,
                    volume = basketVolume,
                    markTypeGroup = markTypeGroup
            ).apply {
                isLocked = restBasket.isClose.isSapTrue()
                isPrinted = restBasket.isPrint.isSapTrue()
                addGoodFormRest(taskGoods, mapOfGoodsByMaterial)
            }
        }

        baskets.addAll(mappedBaskets.orEmpty())
    }

    private fun Basket.addGoodFormRest(taskGoods: List<Good>, mapOfGoodsByMaterial: Map<String, List<BasketPositionInfo>>) {
        //Найдем товар с номером этой корзины с помощью мапы
        val goodToAdd = taskGoods.find { goodFromTask ->
            mapOfGoodsByMaterial[goodFromTask.material]?.any { it.basketNumber == index.toString() } == true
        }
        //Затем найдем его количество в корзине
        goodToAdd?.let { good ->
            val listOfBasketsWithThatGood = mapOfGoodsByMaterial[good.material]
            val goodQuantity = listOfBasketsWithThatGood
                    ?.firstOrNull { it.basketNumber == index.toString() }
                    ?.quantity?.toDoubleOrNull()
                    ?: DEFAULT_QUANTITY
            maxRetailPrice = good.maxRetailPrice
            addGood(good, goodQuantity)
        }
    }


    override fun findGoodByEan(ean: String): Good? {
        return currentTask.value?.let { task ->
            task.goods.find { good ->
                good.ean == ean || good.eans.contains(ean)
            }?.also { found ->
                found.ean = ean
                updateCurrentTask(task)
            }
        }
    }

    override fun findGoodByEanAndMRC(ean: String, mrc: String): Good? {
        return if (mrc.isNotEmpty()) {
            findGoodByEan(ean)
        } else {
            currentTask.value?.let { task ->
                task.goods.find { good ->
                    (good.ean == ean || good.eans.contains(ean)) && (good.maxRetailPrice == mrc)
                }
            }
        }
    }

    override fun findGoodByMaterial(material: String): Good? {
        return currentTask.value?.goods?.find { it.material == material }
    }

    override fun isGoodCorrespondToTask(goodInfo: GoodInfoResult): Boolean {
        currentTask.value?.let { task ->
            val isControl = task.control == goodInfo.getControlType()
            val isType = if (task.goodType.isNotEmpty()) task.goodType == goodInfo.materialInfo?.goodType else true
            val isSection = if (task.section.isNotEmpty()) task.section == goodInfo.materialInfo?.section else true
            val isPurchaseGroup = if (task.purchaseGroup.isNotEmpty()) task.purchaseGroup == goodInfo.materialInfo?.purchaseGroup else true
            val isProvider = if (task.provider.code.orEmpty().isNotEmpty()) goodInfo.providers?.find { it.code == task.provider.code } != null else true

            Logg.d { "--> task parameters: ${task.control} / ${task.goodType} / ${task.section} / ${task.purchaseGroup} / ${task.provider.code}" }
            Logg.d { "--> good parameters: ${goodInfo.getControlType()} / ${goodInfo.materialInfo?.goodType} / ${goodInfo.materialInfo?.section} / ${goodInfo.materialInfo?.purchaseGroup} / ${goodInfo.providers}" }

            return isControl && isType && isSection && isPurchaseGroup && isProvider
        }

        return false
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

    override fun removeBaskets(basketList: MutableList<Basket>) {
        currentTask.value?.let { task ->
            task.removeBaskets(basketList)
            updateCurrentTask(task)
        }
    }

    override fun deleteGoodsFromBaskets(materials: List<String>) {
        currentTask.value?.let { task ->
            materials.forEach { goodNumberToDelete ->
                task.baskets.forEach { basket ->
                    val goodToDelete = basket.getGoodList().firstOrNull { it.material == goodNumberToDelete }
                    goodToDelete?.let {
                        basket.goods.remove(it)
                    }
                }
            }
            task.removeEmptyBaskets()
            updateCurrentTask(task)
        }
    }

    override fun finishCurrentTask() {
        currentTask.value?.let { task ->
            task.isFinished = true
            updateCurrentTask(task)
        }
    }

    override fun prepareSendTaskDataParams(deviceIp: String, tkNumber: String, userNumber: String) {
        currentTask.value?.let { task ->
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
                                section = basket.section,
                                isClose = basket.isLocked.toSapBooleanString(),
                                isPrint = basket.isPrinted.toSapBooleanString()
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
                if (good.isEmpty() || good.isDeleted) {
                    positions.add(
                            PositionInfo(
                                    material = good.material,
                                    providerCode = task.provider.code.orEmpty(),
                                    providerName = task.provider.name.orEmpty(),
                                    factQuantity = "0",
                                    isCounted = good.isCounted.toSapBooleanString(),
                                    isDeleted = good.isDeleted.toSapBooleanString(),
                                    innerQuantity = good.innerQuantity.dropZeros(),
                                    unitsCode = good.commonUnits.code
                            )
                    )
                } else {
                    good.positions.mapTo(positions) { position ->
                        val quantity = if (position.quantity > 0.0) position.quantity else good.getTotalQuantity()
                        PositionInfo(
                                material = good.material,
                                providerCode = position.provider.code.orEmpty(),
                                providerName = position.provider.name.orEmpty(),
                                factQuantity = quantity.dropZeros(),
                                isCounted = good.isCounted.toSapBooleanString(),
                                isDeleted = good.isDeleted.toSapBooleanString(),
                                innerQuantity = good.innerQuantity.dropZeros(),
                                unitsCode = good.commonUnits.code
                        )
                    }

                    good.marks.mapTo(marks) { mark ->
                        ExciseMarkInfo(
                                material = good.material,
                                number = mark.number,
                                boxNumber = mark.boxNumber,
                                isBadMark = mark.isBadMark.toSapBooleanString(),
                                providerCode = mark.providerCode,
                                basketNumber = mark.basketNumber.toString()
                        )
                    }

                    good.parts.mapTo(parts) { part ->
                        PartInfo(
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
            }

            generalTaskManager.setSendTaskDataParams(
                    SendTaskDataParams(
                            deviceIp = deviceIp,
                            taskNumber = task.number,
                            userNumber = userNumber,
                            taskName = task.name,
                            taskType = task.type?.code.orEmpty(),
                            tkNumber = tkNumber,
                            storage = task.storage,
                            reasonCode = task.reason?.code.orEmpty(),
                            isNotFinish = (!task.isFinished).toSapBooleanString(),
                            positions = positions,
                            exciseMarks = marks,
                            parts = parts,
                            baskets = baskets,
                            basketPositions = basketPositions
                    )
            )
        }
    }

    override fun markGoodsDeleted(materials: List<String>) {
        currentTask.value?.let { task ->
            materials.forEach { material ->
                task.goods.find { it.material == material }?.isDeleted = true
            }

            updateCurrentTask(task)
        }
    }

    override fun markGoodsUncounted(materials: List<String>) {
        currentTask.value?.let { task ->
            materials.forEach { material ->
                task.goods.find { it.material == material }?.isCounted = false
            }

            updateCurrentTask(task)
        }
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
                good.marks.removeAll(mappedMarks)
            }
            task.removeEmptyBaskets()
            task.removeEmptyGoods()

            updateCurrentTask(task)
        }
    }

    override fun saveStartTaskInfo() {
        val hashOfCurrentTask = currentTask.value.hashCode()
        startStateHashOfCurrentTask = hashOfCurrentTask
    }

    override fun isExistStartTaskInfo(): Boolean {
        return startStateHashOfCurrentTask != -1
    }

    override fun isTaskWasChanged(): Boolean {
        return if (isExistStartTaskInfo()) {
            currentTask.value.hashCode() != startStateHashOfCurrentTask
        } else false
    }

    override fun clearStartTaskInfo() {
        startStateHashOfCurrentTask = -1
    }


    override fun clearCurrentTask() {
        tasks.value?.let { tasks ->
            currentTask.value?.let { task ->
                tasks.find { it.number == task.number }?.goods?.clear()
            }

            updateCurrentTask(null)
            updateTasks(tasks)
        }
    }

    companion object {
        private const val NULL_BASKET_VOLUME = "Объем корзины отсутствует"
        private const val INDEX_OF_FIRST_BASKET = 1
    }
}