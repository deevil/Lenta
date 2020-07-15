package com.lenta.bp18.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp18.model.pojo.Good
import com.lenta.bp18.model.pojo.Raw
import com.lenta.bp18.model.pojo.Task
import com.lenta.shared.utilities.extentions.isSapTrue
import javax.inject.Inject

class TaskManager @Inject constructor(
        /*private val database: IDatabaseRepository,
        private val persistLabelList: PersistLabelList*/
) : ITaskManager {

/*    override lateinit var taskType: TaskType

    private var labelLimit = 0

    override val labels = MutableLiveData<List<LabelInfo>>(emptyList())

    override val tasks = MutableLiveData<List<Task>>(emptyList())

    override val currentTask = MutableLiveData<Task>()

    override val currentGood = MutableLiveData<Good>()

    override val currentRaw = MutableLiveData<Raw>()


    override fun updateCurrentTask(task: Task?) {
        currentTask.value = task
    }

    override fun updateCurrentGood(good: Good?) {
        currentGood.value = good
    }

    override fun updateCurrentRaw(raw: Raw?) {
        currentRaw.value = raw
    }

    override fun onTaskChanged() {
        currentTask.value?.let { task ->
            currentTask.value = task
        }
    }

    override suspend fun getLabelList() {
        labelLimit = database.getLabelLimit()

        val labelList = persistLabelList.getLabelList().toMutableList()
        while (labelList.size > labelLimit && labelList.isNotEmpty()) {
            labelList.removeAt(labelList.size - 1)
        }

        persistLabelList.saveLabelList(labelList)
        labels.postValue(labelList)
    }

    override suspend fun addLabelToList(labelInfo: LabelInfo) {
        if (labelLimit > 0) {
            labels.value?.let { list ->
                val labelList = list.toMutableList()
                while (labelList.size >= labelLimit) {
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
                            order = rawInfo.orderNumber,
                            name = rawInfo.name,
                            planned = rawInfo.planned,
                            isWasDef = rawInfo.isWasDef.isSapTrue()
                    )
                }

                val packList = taskInfoResult.packs.filter { packInfo ->
                    rawList.any { it.order == packInfo.order } || packInfo.materialDef == goodInfo.material
                }.map { packInfo ->
                    Pack(
                            material = packInfo.material,
                            materialOsn = packInfo.materialOsn,
                            materialDef = packInfo.materialDef,
                            code = packInfo.code,
                            order = packInfo.order,
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

            updateCurrentTask(task)
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

            updateCurrentTask(task)
        }
    }

    override fun getTaskTypeCode(): Int {
        return when (taskType) {
            TaskType.PROCESSING_UNIT -> 1
            TaskType.EXTERNAL_SUPPLY -> 2
        }
    }

    override fun getBlockType(): Int {
        return when (taskType) {
            TaskType.PROCESSING_UNIT -> 1
            TaskType.EXTERNAL_SUPPLY -> 3
        }
    }*/

}

interface ITaskManager {

    /*var taskType: TaskType

    val tasks: MutableLiveData<List<Task>>
    val labels: MutableLiveData<List<LabelInfo>>
    val currentTask: MutableLiveData<Task>
    val currentGood: MutableLiveData<Good>
    val currentRaw: MutableLiveData<Raw>

    fun updateCurrentTask(task: Task?)
    fun updateCurrentGood(good: Good?)
    fun updateCurrentRaw(raw: Raw?)

    fun onTaskChanged()

    fun addTasks(taskListResult: TaskListResult)
    suspend fun addTaskInfoToCurrentTask(taskInfoResult: TaskInfoResult)
    fun getTaskTypeCode(): Int
    fun getBlockType(): Int
    fun completeCurrentTask()
    fun completeCurrentGood()
    suspend fun getLabelList()
    suspend fun addLabelToList(labelInfo: LabelInfo)*/

}
