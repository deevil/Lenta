package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.Block
import com.lenta.bp12.model.pojo.open_task.Position
import com.lenta.bp12.model.pojo.Properties
import com.lenta.bp12.model.pojo.open_task.Good
import com.lenta.bp12.model.pojo.open_task.Task
import com.lenta.bp12.platform.extention.addZerosToStart
import com.lenta.bp12.platform.extention.getBlockType
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoResult
import com.lenta.bp12.request.SendTaskDataParams
import com.lenta.bp12.request.TaskContentResult
import com.lenta.bp12.request.pojo.*
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.extentions.toSapBooleanString
import javax.inject.Inject

class OpenTaskManager @Inject constructor(
        private val database: IDatabaseRepository,
        private val generalTaskManager: IGeneralTaskManager
) : IOpenTaskManager {

    override var searchNumber = ""

    override val searchParams = MutableLiveData<TaskSearchParams>()

    override val tasks = MutableLiveData<List<Task>>(emptyList())

    override val foundTasks = MutableLiveData<List<Task>>(emptyList())

    override val currentTask = MutableLiveData<Task>()

    override val currentGood = MutableLiveData<Good>()

    override val currentPosition = MutableLiveData<Position>()


    override fun updateTasks(taskList: List<Task>?) {
        tasks.value = taskList ?: emptyList()
    }

    override fun updateFoundTasks(taskList: List<Task>?) {
        foundTasks.value = taskList ?: emptyList()
    }

    override fun updateCurrentTask(task: Task?) {
        currentTask.value = task
    }

    override fun updateCurrentGood(good: Good?) {
        currentGood.value = good
    }

    override fun updateCurrentPosition(position: Position?) {
        currentPosition.value = position
    }

    override suspend fun putInCurrentGood(goodInfo: GoodInfoResult) {
        val newGood = Good(
                ean = goodInfo.eanInfo.ean,
                material = goodInfo.materialInfo.material,
                name = goodInfo.materialInfo.name,
                units = database.getUnitsByCode(goodInfo.materialInfo.unitsCode),
                kind = goodInfo.getGoodKind(),
                type = goodInfo.materialInfo.goodType,
                control = goodInfo.getControlType(),
                section = goodInfo.materialInfo.section,
                matrix = getMatrixType(goodInfo.materialInfo.matrix),
                isFullData = true,
                providers = goodInfo.providers.toMutableList(),
                producers = goodInfo.producers.toMutableList()
        )

        findGoodByMaterial(newGood.material)?.let { good ->
            newGood.positions = good.positions
        }

        currentGood.value = newGood
    }

    override fun addCurrentGoodInTask() {
        currentTask.value?.let { task ->
            task.goods.find { it.material == currentGood.value!!.material }?.let { good ->
                task.goods.remove(good)
            }

            task.goods.add(0, currentGood.value!!)
            updateCurrentTask(task)
        }
    }

    override suspend fun addTasks(tasksInfo: List<TaskInfo>) {
        val taskList = tasksInfo.map { taskInfo ->
            Task(
                    number = taskInfo.number,
                    name = taskInfo.name,
                    properties = Properties(
                            type = taskInfo.type,
                            description = taskInfo.name,
                            section = taskInfo.section,
                            purchaseGroup = taskInfo.purchaseGroup,
                            goodGroup = taskInfo.goodGroup
                    ),
                    storage = taskInfo.storage,
                    isStrict = taskInfo.isStrict.isSapTrue(),
                    block = Block(
                            type = taskInfo.blockType.getBlockType(),
                            user = taskInfo.blockUser,
                            ip = taskInfo.blockIp
                    ),
                    isFinished = !taskInfo.isNotFinish.isSapTrue(),
                    control = taskInfo.control.getControlType(),
                    comment = taskInfo.comment,
                    provider = ProviderInfo(
                            code = taskInfo.providerCode.addZerosToStart(10),
                            name = taskInfo.providerName
                    ),
                    quantity = taskInfo.quantity.toIntOrNull() ?: 0,
                    reason = database.getReturnReasonList(taskInfo.type).first { it.code == taskInfo.reasonCode }
            )
        }

        updateTasks(taskList)
    }

    override suspend fun addFoundTasks(tasksInfo: List<TaskInfo>) {
        val taskList = tasksInfo.map { taskInfo ->
            Task(
                    number = taskInfo.number,
                    name = taskInfo.name,
                    properties = Properties(
                            type = taskInfo.type,
                            description = taskInfo.name,
                            section = taskInfo.section,
                            purchaseGroup = taskInfo.purchaseGroup,
                            goodGroup = taskInfo.goodGroup
                    ),
                    storage = taskInfo.storage,
                    isStrict = taskInfo.isStrict.isSapTrue(),
                    block = Block(
                            type = taskInfo.blockType.getBlockType(),
                            user = taskInfo.blockUser,
                            ip = taskInfo.blockIp
                    ),
                    isFinished = !taskInfo.isNotFinish.isSapTrue(),
                    control = taskInfo.control.getControlType(),
                    comment = taskInfo.comment,
                    provider = ProviderInfo(
                            code = taskInfo.providerCode.addZerosToStart(10),
                            name = taskInfo.providerName
                    ),
                    quantity = taskInfo.quantity.toIntOrNull() ?: 0,
                    reason = database.getReturnReasonList(taskInfo.type).first { it.code == taskInfo.reasonCode }
            )
        }

        updateFoundTasks(taskList)
    }

    override suspend fun addGoodsInCurrentTask(taskContentResult: TaskContentResult) {
        currentTask.value?.let { task ->
            taskContentResult.positions.map { positionInfo ->
                val position = Position(
                        innerQuantity = positionInfo.innerQuantity.toDoubleOrNull() ?: 1.0,
                        provider = ProviderInfo(
                                name = positionInfo.providerName,
                                code = positionInfo.providerCode.addZerosToStart(10)
                        ),
                        units = database.getUnitsByCode(positionInfo.unitsCode),
                        isCounted = positionInfo.isCounted.isSapTrue(),
                        isDelete = positionInfo.isDeleted.isSapTrue()
                )

                val good = task.goods.find { it.isSameMaterial(positionInfo.material) }
                        ?: database.getGoodByMaterial(positionInfo.material)

                good?.positions?.add(0, position)

                task.updateGood(good)
            }

            updateCurrentTask(task)
        }
    }

    override fun findGoodByEan(ean: String): Good? {
        return currentTask.value?.goods?.find { it.ean == ean }
    }

    override fun findGoodByMaterial(material: String): Good? {
        val formattedMaterial = if (material.length == Constants.SAP_6) "000000000000$material" else material
        return currentTask.value?.goods?.find { it.material == formattedMaterial }
    }

    override suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean {
        return database.isGoodCanBeAdded(goodInfo, currentTask.value!!.properties!!.type)
    }

    override fun finishCurrentTask() {
        currentTask.value?.let { task ->
            task.isFinished = true

            updateCurrentTask(task)
        }
    }

    override fun addProviderInCurrentGood(providerInfo: ProviderInfo) {
        currentGood.value?.let { good ->
            good.providers.add(0, providerInfo)

            updateCurrentGood(good)
        }
    }

    override fun preparePositionToOpen(material: String, providerCode: String) {
        findGoodByMaterial(material)?.let { good ->
            updateCurrentGood(good)

            good.positions.find { it.provider.code == providerCode }?.let { position ->
                updateCurrentPosition(position)
            }
        }
    }

    override fun prepareSendTaskDataParams(deviceIp: String, tkNumber: String, userNumber: String) {
        currentTask.value?.let { task ->
            val positions = mutableListOf<PositionInfo>()
            val marks = mutableListOf<MarkInfo>()
            val parts = mutableListOf<PartInfo>()

            task.goods.forEach { good ->
                good.positions.forEach { position ->
                    positions.add(
                            PositionInfo(
                                    material = good.material,
                                    providerCode = position.provider.code,
                                    providerName = position.provider.name,
                                    quantity = position.quantity.dropZeros(),
                                    isCounted = position.isCounted.toSapBooleanString(),
                                    isDeleted = position.isDelete.toSapBooleanString(),
                                    unitsCode = good.units.code
                            )
                    )
                }

                good.marks.map { mark ->
                    marks.add(
                            MarkInfo(
                                    material = good.material,
                                    markNumber = mark.markNumber,
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
                                    producer = part.producer,
                                    productionDate = part.productionDate,
                                    unitsCode = part.units.code,
                                    quantity = part.quantity.dropZeros(),
                                    partNumber = part.partNumber,
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
                            taskType = task.properties!!.type,
                            tkNumber = tkNumber,
                            storage = task.storage,
                            reasonCode = task.reason.code,
                            isNotFinish = (!task.isFinished).toSapBooleanString(),
                            positions = positions,
                            marks = marks,
                            parts = parts
                    )
            )
        }
    }

    override fun markPositionsDelete(items: List<SimplePosition>) {
        currentTask.value?.let { task ->
            items.forEach { item ->
                task.goods.find { it.material == item.material }?.markPositionDelete(item.providerCode)
            }

            updateCurrentTask(task)
        }
    }

    override fun markPositionsUncounted(items: List<SimplePosition>) {
        currentTask.value?.let { task ->
            items.forEach { item ->
                task.goods.find { it.material == item.material }?.markPositionUncounted(item.providerCode)
            }

            updateCurrentTask(task)
        }
    }

    override fun markPositionsMissing(items: List<SimplePosition>) {
        currentTask.value?.let { task ->
            items.forEach { item ->
                task.goods.find { it.material == item.material }?.markPositionMissing(item.providerCode)
            }

            updateCurrentTask(task)
        }
    }

}


interface IOpenTaskManager {

    var searchNumber: String

    val searchParams: MutableLiveData<TaskSearchParams>

    val tasks: MutableLiveData<List<Task>>
    val foundTasks: MutableLiveData<List<Task>>

    val currentTask: MutableLiveData<Task>
    val currentGood: MutableLiveData<Good>
    val currentPosition: MutableLiveData<Position>

    fun updateTasks(taskList: List<Task>?)
    fun updateFoundTasks(taskList: List<Task>?)
    fun updateCurrentTask(task: Task?)
    fun updateCurrentGood(good: Good?)
    fun updateCurrentPosition(position: Position?)

    suspend fun putInCurrentGood(goodInfo: GoodInfoResult)
    fun addCurrentGoodInTask()
    fun findGoodByEan(ean: String): Good?
    fun findGoodByMaterial(material: String): Good?
    suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean
    fun finishCurrentTask()
    fun addProviderInCurrentGood(providerInfo: ProviderInfo)
    suspend fun addTasks(tasksInfo: List<TaskInfo>)
    suspend fun addFoundTasks(tasksInfo: List<TaskInfo>)
    suspend fun addGoodsInCurrentTask(taskContentResult: TaskContentResult)
    fun preparePositionToOpen(material: String, providerCode: String)
    fun prepareSendTaskDataParams(deviceIp: String, tkNumber: String, userNumber: String)
    fun markPositionsDelete(items: List<SimplePosition>)
    fun markPositionsUncounted(items: List<SimplePosition>)
    fun markPositionsMissing(items: List<SimplePosition>)

}

data class SimplePosition(
        val material: String,
        val providerCode: String
)