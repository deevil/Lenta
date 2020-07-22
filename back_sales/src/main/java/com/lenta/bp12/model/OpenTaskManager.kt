package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.Block
import com.lenta.bp12.model.pojo.open_task.GoodOpen
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoResult
import com.lenta.bp12.request.SendTaskDataParams
import com.lenta.bp12.request.TaskContentResult
import com.lenta.bp12.request.pojo.*
import com.lenta.shared.models.core.getInnerUnits
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.extentions.toSapBooleanString
import com.lenta.shared.utilities.getStringFromDate
import javax.inject.Inject

class OpenTaskManager @Inject constructor(
        private val database: IDatabaseRepository,
        private val generalTaskManager: IGeneralTaskManager
) : IOpenTaskManager {

    override var searchNumber = ""

    override var searchGoodFromList = false

    override val searchParams = MutableLiveData<TaskSearchParams>()

    override val tasks = MutableLiveData<List<TaskOpen>>(emptyList())

    override val foundTasks = MutableLiveData<List<TaskOpen>>(emptyList())

    override val currentTask = MutableLiveData<TaskOpen>()

    override val currentGood = MutableLiveData<GoodOpen>()


    override fun updateTasks(taskList: List<TaskOpen>?) {
        tasks.value = taskList ?: emptyList()
    }

    override fun updateFoundTasks(taskList: List<TaskOpen>?) {
        foundTasks.value = taskList ?: emptyList()
    }

    override fun updateCurrentTask(task: TaskOpen?) {
        currentTask.value = task
    }

    override fun updateCurrentGood(good: GoodOpen) {
        currentGood.value = good
    }

    override fun clearCurrentGood() {
        currentGood.value = null
    }

    override fun saveGoodInTask(good: GoodOpen) {
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
            TaskOpen(
                    number = taskInfo.number,
                    name = taskInfo.name,
                    type = database.getTaskType(taskInfo.typeCode),
                    block = Block(
                            type = BlockType.from(taskInfo.blockType),
                            user = taskInfo.blockUser,
                            ip = taskInfo.blockIp
                    ),
                    storage = taskInfo.storage,
                    control = ControlType.from(taskInfo.control),
                    provider = ProviderInfo(
                            code = taskInfo.providerCode,
                            name = taskInfo.providerName
                    ),
                    reason = database.getReturnReason(taskInfo.typeCode, taskInfo.reasonCode),
                    comment = taskInfo.comment,
                    section = taskInfo.section,
                    goodType = taskInfo.goodType,
                    purchaseGroup = taskInfo.purchaseGroup,
                    goodGroup = taskInfo.goodType,
                    numberOfGoods = taskInfo.quantity.toIntOrNull() ?: 0,
                    isStrict = taskInfo.isStrict.isSapTrue(),
                    isFinished = !taskInfo.isNotFinish.isSapTrue()
            )
        }
    }

    override suspend fun addGoodsInCurrentTask(taskContentResult: TaskContentResult) {
        currentTask.value?.let { task ->
            taskContentResult.positions.map { positionInfo ->
                with(positionInfo) {
                    database.getGoodInfoByMaterial(material)?.let { goodInfo ->
                        val commonUnits = database.getUnitsByCode(unitsCode)
                        val good = GoodOpen(
                                ean = goodInfo.ean,
                                allGoodEans = goodInfo.allGoodEans,
                                material = material,
                                name = goodInfo.name,
                                section = goodInfo.section,
                                matrix = goodInfo.matrix,
                                kind = goodInfo.kind,
                                planQuantity = planQuantity.toDoubleOrNull() ?: 0.0,
                                factQuantity = factQuantity.toDoubleOrNull() ?: 0.0,
                                commonUnits = commonUnits,
                                innerUnits = getInnerUnits(commonUnits),
                                innerQuantity = innerQuantity.toDoubleOrNull() ?: 1.0,
                                isCounted = isCounted.isSapTrue(),
                                isDeleted = isDeleted.isSapTrue(),
                                provider = ProviderInfo(providerCode, providerName),
                                producers = taskContentResult.producers.filter { it.material == goodInfo.material }.map {
                                    ProducerInfo(
                                            code = it.code,
                                            name = it.name
                                    )
                                }
                        )

                        task.goods.add(good)

                        Logg.d { "--> added good = $good" }
                    }
                }
            }

            updateCurrentTask(task)
        }
    }

    override fun findGoodByEan(ean: String): GoodOpen? {
        return currentTask.value?.let { task ->
            task.goods.find { good ->
                good.allGoodEans.contains(ean)
            }?.also { found ->
                found.ean = ean
                updateCurrentTask(task)
            }
        }
    }

    override fun findGoodByMaterial(material: String): GoodOpen? {
        val formattedMaterial = if (material.length == Constants.SAP_6) "000000000000$material" else material
        return currentTask.value?.goods?.find { it.material == formattedMaterial }
    }

    override fun isGoodExist(number: String): Boolean {
        currentTask.value?.let { task ->
            task.goods.find { good ->
                good.material.contains(number)
            }?.also {
                return true
            }

            task.goods.find { good ->
                good.allGoodEans.contains(number)
            }?.also { found ->
                found.ean = number
                updateCurrentTask(task)
                return true
            }
        }

        return false
    }

    override fun isGoodCorrespondToTask(goodInfo: GoodInfoResult): Boolean {
        currentTask.value?.let { task ->
            val isControl = task.control == goodInfo.getControlType()
            val isType = if (task.goodType.isNotEmpty()) task.goodType == goodInfo.materialInfo.goodType else true
            val isSection = if (task.section.isNotEmpty()) task.section == goodInfo.materialInfo.section else true
            val isPurchaseGroup = if (task.purchaseGroup.isNotEmpty()) task.purchaseGroup == goodInfo.materialInfo.purchaseGroup else true
            val isProvider = if (task.provider.code.isNotEmpty()) goodInfo.providers.find { it.code == task.provider.code } != null else true

            Logg.d { "--> task parameters: ${task.control} / ${task.goodType} / ${task.section} / ${task.purchaseGroup} / ${task.provider.code}" }
            Logg.d { "--> good parameters: ${goodInfo.getControlType()} / ${goodInfo.materialInfo.goodType} / ${goodInfo.materialInfo.section} / ${goodInfo.materialInfo.purchaseGroup} / ${goodInfo.providers}" }

            return isControl && isType && isSection && isPurchaseGroup && isProvider
        }

        return false
    }

    override suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean {
        return database.isGoodCanBeAdded(goodInfo, currentTask.value?.type?.code.orEmpty())
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
            val marks = mutableListOf<MarkInfo>()
            val parts = mutableListOf<PartInfo>()

            task.goods.forEach { good ->
                val isDeleted = good.isDeleted

                good.positions.forEach { position ->
                    positions.add(
                            PositionInfo(
                                    material = good.material,
                                    providerCode = position.provider.code,
                                    factQuantity = if (isDeleted) "0" else position.quantity.dropZeros(),
                                    isCounted = good.isCounted.toSapBooleanString(),
                                    isDeleted = isDeleted.toSapBooleanString(),
                                    innerQuantity = good.innerQuantity.dropZeros(),
                                    unitsCode = good.commonUnits.code
                            )
                    )
                }

                good.marks.map { mark ->
                    marks.add(
                            MarkInfo(
                                    material = good.material,
                                    number = mark.number,
                                    boxNumber = mark.boxNumber,
                                    isBadMark = mark.isBadMark.toSapBooleanString(),
                                    providerCode = mark.providerCode
                            )
                    )
                }

                good.parts.map { part ->
                    parts.add(
                            PartInfo(
                                    material = good.material,
                                    producerCode = part.producerCode,
                                    productionDate = getStringFromDate(part.date, Constants.DATE_FORMAT_yyyyMMdd),
                                    quantity = part.quantity.dropZeros(),
                                    partNumber = part.number,
                                    providerCode = part.providerCode
                            )
                    )
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
                            marks = marks,
                            parts = parts
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

    override fun clearSearchFromListParams() {
        searchGoodFromList = false
        searchNumber = ""
    }

}


interface IOpenTaskManager {

    var searchNumber: String
    var searchGoodFromList: Boolean

    val searchParams: MutableLiveData<TaskSearchParams>
    val tasks: MutableLiveData<List<TaskOpen>>
    val foundTasks: MutableLiveData<List<TaskOpen>>
    val currentTask: MutableLiveData<TaskOpen>
    val currentGood: MutableLiveData<GoodOpen>

    fun updateTasks(taskList: List<TaskOpen>?)
    fun updateFoundTasks(taskList: List<TaskOpen>?)
    fun updateCurrentTask(task: TaskOpen?)
    fun updateCurrentGood(good: GoodOpen)

    fun saveGoodInTask(good: GoodOpen)
    fun findGoodByEan(ean: String): GoodOpen?
    fun findGoodByMaterial(material: String): GoodOpen?
    fun isGoodExist(number: String): Boolean
    fun isGoodCorrespondToTask(goodInfo: GoodInfoResult): Boolean
    suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean
    fun finishCurrentTask()
    suspend fun addTasks(tasksInfo: List<TaskInfo>)
    suspend fun addFoundTasks(tasksInfo: List<TaskInfo>)
    suspend fun addGoodsInCurrentTask(taskContentResult: TaskContentResult)
    fun prepareSendTaskDataParams(deviceIp: String, tkNumber: String, userNumber: String)
    fun markGoodsDeleted(materials: List<String>)
    fun markGoodsUncounted(materials: List<String>)
    fun clearSearchFromListParams()
    fun clearCurrentGood()

}