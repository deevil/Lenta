package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.Block
import com.lenta.bp12.model.pojo.open_task.GoodOpen
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.platform.extention.addZerosToStart
import com.lenta.bp12.platform.extention.getBlockType
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoResult
import com.lenta.bp12.request.SendTaskDataParams
import com.lenta.bp12.request.TaskContentResult
import com.lenta.bp12.request.pojo.*
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

    override fun updateCurrentGood(good: GoodOpen?) {
        currentGood.value = good
    }

    /*override suspend fun putInCurrentGood(goodInfo: GoodInfoResult) {
        val newGood = GoodOpen(
                ean = goodInfo.eanInfo.ean,
                material = goodInfo.materialInfo.material,
                name = goodInfo.materialInfo.name,
                units = database.getUnitsByCode(goodInfo.materialInfo.unitsCode),
                type = goodInfo.getGoodType(),
                matype = goodInfo.materialInfo.matype,
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
    }*/

    /*override fun addCurrentGoodInTask() {
        currentTask.value?.let { task ->
            task.goods.find { it.material == currentGood.value!!.material }?.let { good ->
                task.goods.remove(good)
            }

            task.goods.add(0, currentGood.value!!)
            updateCurrentTask(task)
        }
    }*/

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
                            type = taskInfo.blockType.getBlockType(),
                            user = taskInfo.blockUser,
                            ip = taskInfo.blockIp
                    ),
                    storage = taskInfo.storage,
                    control = taskInfo.control.getControlType(),
                    provider = ProviderInfo(
                            code = taskInfo.providerCode.addZerosToStart(10),
                            name = taskInfo.providerName
                    ),
                    reason = database.getReturnReason(taskInfo.typeCode, taskInfo.reasonCode),
                    comment = taskInfo.comment,
                    section = taskInfo.section,
                    purchaseGroup = taskInfo.purchaseGroup,
                    goodGroup = taskInfo.goodGroup,
                    numberOfGoods = taskInfo.quantity.toIntOrNull() ?: 0,
                    isStrict = taskInfo.isStrict.isSapTrue(),
                    isFinished = !taskInfo.isNotFinish.isSapTrue()
            )
        }
    }

    override suspend fun addGoodsInCurrentTask(taskContentResult: TaskContentResult) {
        currentTask.value?.let { task ->
            taskContentResult.positions.map { positionInfo ->
                positionInfo.apply {
                    database.getGoodInfoByMaterial(material)?.let { goodInfo ->
                        task.goods.add(GoodOpen(
                                ean = goodInfo.ean,
                                material = material,
                                name = goodInfo.name,
                                section = goodInfo.section,
                                matrix = goodInfo.matrix,
                                type = goodInfo.type,
                                planQuantity = planQuantity.toDoubleOrNull() ?: 0.0,
                                factQuantity = factQuantity.toDoubleOrNull() ?: 0.0,
                                innerQuantity = innerQuantity.toDoubleOrNull() ?: 1.0,
                                units = database.getUnitsByCode(unitsCode),
                                isCounted = isCounted.isSapTrue(),
                                isDeleted = isDeleted.isSapTrue(),
                                provider = ProviderInfo(providerCode, providerName),
                                producers = taskContentResult.producers.filter { it.material == goodInfo.material }.map {
                                    ProducerInfo(
                                            code = it.code,
                                            name = it.name
                                    )
                                }
                        ))
                    }
                }
            }

            updateCurrentTask(task)
        }
    }

    override fun findGoodByEan(ean: String): GoodOpen? {
        return currentTask.value?.goods?.find { it.ean == ean }
    }

    override fun findGoodByMaterial(material: String): GoodOpen? {
        val formattedMaterial = if (material.length == Constants.SAP_6) "000000000000$material" else material
        return currentTask.value?.goods?.find { it.material == formattedMaterial }
    }

    override suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean {
        return database.isGoodCanBeAdded(goodInfo, currentTask.value?.type?.code ?: "")
    }

    override fun finishCurrentTask() {
        currentTask.value?.let { task ->
            task.isFinished = true

            updateCurrentTask(task)
        }
    }

    /*override fun addProviderInCurrentGood(providerInfo: ProviderInfo) {
        currentGood.value?.let { good ->
            good.providers.add(0, providerInfo)

            updateCurrentGood(good)
        }
    }*/

    /*override fun preparePositionToOpen(material: String, providerCode: String) {
        findGoodByMaterial(material)?.let { good ->
            updateCurrentGood(good)

            good.positions.find { it.provider.code == providerCode }?.let { position ->
                updateCurrentPosition(position)
            }
        }
    }*/

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
                                    factQuantity = position.quantity.dropZeros(),
                                    isCounted = good.isCounted.toSapBooleanString(),
                                    isDeleted = good.isDeleted.toSapBooleanString(),
                                    unitsCode = good.units.code
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
                                    producerCode = mark.producerCode
                            )
                    )
                }

                good.parts.map { part ->
                    parts.add(
                            PartInfo(
                                    material = good.material,
                                    producerCode = part.producerCode,
                                    productionDate = part.date,
                                    unitsCode = part.units.code,
                                    factQuantity = part.quantity.dropZeros(),
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
                            taskType = task.type?.code ?: "",
                            tkNumber = tkNumber,
                            storage = task.storage,
                            reasonCode = task.reason?.code ?: "",
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

    /*override fun markPositionsMissing(items: List<SimplePosition>) {
        currentTask.value?.let { task ->
            items.forEach { item ->
                task.goods.find { it.material == item.material }?.markPositionMissing(item.providerCode)
            }

            updateCurrentTask(task)
        }
    }*/

    /*override fun clearCurrentGoodAndPosition() {
        currentGood.value = null
        currentPosition.value = null
    }*/

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
    fun updateCurrentGood(good: GoodOpen?)

    fun saveGoodInTask(good: GoodOpen)
    //suspend fun putInCurrentGood(goodInfo: GoodInfoResult)
    //fun addCurrentGoodInTask()
    fun findGoodByEan(ean: String): GoodOpen?
    fun findGoodByMaterial(material: String): GoodOpen?
    suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean
    fun finishCurrentTask()
    //fun addProviderInCurrentGood(providerInfo: ProviderInfo)
    suspend fun addTasks(tasksInfo: List<TaskInfo>)
    suspend fun addFoundTasks(tasksInfo: List<TaskInfo>)
    suspend fun addGoodsInCurrentTask(taskContentResult: TaskContentResult)
    //fun preparePositionToOpen(material: String, providerCode: String)
    fun prepareSendTaskDataParams(deviceIp: String, tkNumber: String, userNumber: String)
    fun markGoodsDeleted(materials: List<String>)
    fun markGoodsUncounted(materials: List<String>)
    fun clearSearchFromListParams()
    //fun markPositionsMissing(items: List<SimplePosition>)
    //fun clearCurrentGoodAndPosition()

}

data class SimplePosition(
        val material: String,
        val providerCode: String
)
