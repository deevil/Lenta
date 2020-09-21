package com.lenta.bp12.managers

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.managers.base.BaseTaskManager
import com.lenta.bp12.managers.interfaces.IGeneralTaskManager
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Position
import com.lenta.bp12.model.pojo.extentions.addGood
import com.lenta.bp12.model.pojo.extentions.addPosition
import com.lenta.bp12.model.pojo.extentions.getGoodList
import com.lenta.bp12.model.pojo.extentions.getQuantityOfGood
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.bp12.platform.ZERO_VOLUME
import com.lenta.bp12.platform.extention.getControlType
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
import javax.inject.Inject

class OpenTaskManager @Inject constructor(
        override val database: IDatabaseRepository,
        private val generalTaskManager: IGeneralTaskManager
) : BaseTaskManager<TaskOpen>(), IOpenTaskManager {

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

    override fun updateTasks(taskList: List<TaskOpen>?) {
        tasks.value = taskList ?: emptyList()
    }

    override fun updateFoundTasks(taskList: List<TaskOpen>?) {
        foundTasks.value = taskList ?: emptyList()
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
                            planQuantity = planQuantity?.toDoubleOrNull() ?: ZERO_QUANTITY,
                            factQuantity = factQuantity?.toDoubleOrNull() ?: ZERO_QUANTITY,
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
                            type = "",
                            purchaseGroup = purchaseGroup
                    )

                    factQuantity?.toDoubleOrNull()?.let { factQuantity ->
                        if (factQuantity != ZERO_QUANTITY) {
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
                    markTypeGroup = markTypeGroup,
                    purchaseGroup = restBasket.purchaseGroup,
                    mprGroup = restBasket.groupMpr
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
                    ?: ZERO_QUANTITY
            maxRetailPrice = good.maxRetailPrice
            addGood(good, goodQuantity)
        }
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
}