package com.lenta.bp16.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.data.LabelInfo
import com.lenta.bp16.model.pojo.Good
import com.lenta.bp16.model.pojo.Pack
import com.lenta.bp16.model.pojo.Raw
import com.lenta.bp16.model.pojo.Task
import com.lenta.bp16.platform.extention.getTaskStatus
import com.lenta.bp16.repository.IDatabaseRepository
import com.lenta.bp16.request.TaskInfoResult
import com.lenta.bp16.request.TaskListResult
import com.lenta.shared.utilities.extentions.isSapTrue
import javax.inject.Inject

class TaskManager @Inject constructor(
        private val database: IDatabaseRepository,
        private val persistLabelList: PersistLabelList
) : ITaskManager {

    override lateinit var taskType: TaskType

    private var labelLimit = 0

    override val labels = MutableLiveData<List<LabelInfo>>(emptyList())

    override val tasks = MutableLiveData<List<Task>>(emptyList())

    override val currentTask = MutableLiveData<Task>()

    override val currentGood = MutableLiveData<Good>()

    override val currentRaw = MutableLiveData<Raw>()


    override suspend fun getLabelList() {
        labelLimit = database.getLabelLimit()

        val labelList = persistLabelList.getLabelList().toMutableList()
        while (labelList.size > labelLimit && labelList.isNotEmpty()) {
            labelList.removeAt(labelList.size - 1)
        }

        labels.postValue(labelList)
    }

    override suspend fun addLabelToList(labelInfo: LabelInfo) {
        if (labelLimit > 0) {
            labels.value?.let { list ->
                val labelList = list.toMutableList()
                if (labelList.size == labelLimit) {
                    labelList.removeAt(labelList.size - 1)
                }

                labelList.add(0, labelInfo)
                persistLabelList.saveLabelList(labelList)
                labels.postValue(labelList)
            }
        }
    }

    override fun addTasks(taskListResult: TaskListResult) {
        val taskList = tasks.value!!.filter { it.isProcessed }.toMutableList()
        taskListResult.tasks.forEach { taskInfo ->
            val processedTask = taskList.find { it.taskInfo.number == taskInfo.number }
            if (processedTask == null) {
                val position = if (taskInfo.isPack.isSapTrue()) 0 else taskList.size
                taskList.add(position, Task(
                        number = taskInfo.number,
                        status = taskInfo.getTaskStatus(),
                        isPack = taskInfo.isPack.isSapTrue(),
                        taskInfo = taskInfo,
                        type = taskType,
                        quantity = taskInfo.quantity.toDoubleOrNull() ?: 0.0
                ))
            }
        }

        tasks.value = taskList
    }

    override suspend fun addTaskInfoToCurrentTask(taskInfoResult: TaskInfoResult) {
        currentTask.value?.let { task ->
            task.goods = taskInfoResult.goods.map { goodInfo ->
                val rawList = taskInfoResult.raws.filter { it.material == goodInfo.material }.map { rawInfo ->
                    Raw(
                            material = rawInfo.material,
                            materialOsn = rawInfo.materialOsn,
                            orderNumber = rawInfo.orderNumber,
                            name = rawInfo.name,
                            planned = rawInfo.planned,
                            isWasDef = rawInfo.isWasDef.isSapTrue()
                    )
                }

                val packList = taskInfoResult.packs.filter { packInfo ->
                    rawList.any { it.orderNumber == packInfo.orderNumber }
                }.map { packInfo ->
                    Pack(
                            material = packInfo.material,
                            materialOsn = packInfo.materialOsn,
                            materialDef = packInfo.materialDef,
                            code = packInfo.code,
                            orderNumber = packInfo.orderNumber,
                            quantity = packInfo.quantity,
                            isDefOut = packInfo.isDefOut.isSapTrue(),
                            category = database.getCategory(packInfo.categoryCode),
                            defect = database.getDefect(packInfo.defectCode)
                    )
                }

                Good(
                        material = goodInfo.material,
                        name = goodInfo.name,
                        units = database.getUnitsByCode(goodInfo.unitsCode),
                        arrived = goodInfo.quantity,
                        raws = rawList.toMutableList(),
                        packs = packList.toMutableList()
                )
            }.toMutableList()

            currentTask.value = task
        }
    }

    override fun completeCurrentTask() {
        tasks.value?.let { list ->
            list.find { task -> task.number == currentTask.value?.number }?.let { currentTask ->
                currentTask.isProcessed = true
                currentTask.status = TaskStatus.COMMON
            }

            tasks.value = list
        }
    }

    override fun completeCurrentGood() {
        currentTask.value?.let { task ->
            task.goods.find { it.material == currentGood.value?.material }?.let { good ->
                good.isProcessed = true
            }

            currentTask.value = task
        }
    }

    override fun getTaskTypeCode(): Int {
        return when (taskType) {
            TaskType.PROCESSING_UNIT -> 1
            TaskType.EXTERNAL_SUPPLY -> 2
        }
    }

    override fun getBlockType(): Int {
        //todo Добавить варианты с переблокировкой

        return when (taskType) {
            TaskType.PROCESSING_UNIT -> 1
            TaskType.EXTERNAL_SUPPLY -> 3
        }
    }

    override fun updateGoodInCurrentTask(good: Good) {
        currentTask.value?.let { task ->
            val index = task.goods.indexOf(task.goods.find { it.material == currentGood.value?.material })
            if (index >= 0) {
                task.goods.removeAt(index)
            }

            task.goods.add(good)
            currentTask.value = task
        }
    }

    override fun onTaskChanged() {
        currentTask.value?.let { task ->
            currentTask.value = task
        }
    }

}

interface ITaskManager {
    var taskType: TaskType

    val tasks: MutableLiveData<List<Task>>
    val labels: MutableLiveData<List<LabelInfo>>
    val currentTask: MutableLiveData<Task>
    val currentGood: MutableLiveData<Good>
    val currentRaw: MutableLiveData<Raw>

    fun addTasks(taskListResult: TaskListResult)
    suspend fun addTaskInfoToCurrentTask(taskInfoResult: TaskInfoResult)
    fun getTaskTypeCode(): Int
    fun getBlockType(): Int
    fun completeCurrentTask()
    fun completeCurrentGood()
    fun onTaskChanged()
    suspend fun getLabelList()
    suspend fun addLabelToList(labelInfo: LabelInfo)
    fun updateGoodInCurrentTask(good: Good)
}