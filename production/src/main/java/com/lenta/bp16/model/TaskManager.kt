package com.lenta.bp16.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.pojo.Good
import com.lenta.bp16.model.pojo.Pack
import com.lenta.bp16.model.pojo.Raw
import com.lenta.bp16.model.pojo.Task
import com.lenta.bp16.platform.extention.getTaskStatus
import com.lenta.bp16.repository.IGeneralRepository
import com.lenta.bp16.request.TaskInfoResult
import com.lenta.bp16.request.TaskListResult
import javax.inject.Inject

class TaskManager @Inject constructor(
        private val repository: IGeneralRepository
) : ITaskManager {

    override lateinit var taskType: TaskType

    override val tasks = MutableLiveData<List<Task>>(emptyList())

    override val currentTask = MutableLiveData<Task>()

    override val currentGood = MutableLiveData<Good>()

    override val currentRaw = MutableLiveData<Raw>()

    override fun addTasks(taskListResult: TaskListResult) {
        val taskList = tasks.value!!.filter { it.isProcessed }.toMutableList()
        taskListResult.tasks.forEach { taskInfo ->
            val processedTask = taskList.find { it.taskInfo.number == taskInfo.number }
            if (processedTask == null) {
                taskList.add(Task(
                        number = taskInfo.number,
                        status = taskInfo.getTaskStatus(),
                        taskInfo = taskInfo,
                        type = taskType,
                        quantity = taskInfo.quantity.toIntOrNull() ?: 0
                ))
            }
        }

        tasks.value = taskList
    }

    override suspend fun addTaskInfoToCurrentTask(taskInfoResult: TaskInfoResult) {
        currentTask.value?.let { task ->
            task.goods = taskInfoResult.goods.map { goodInfo ->
                Good(
                        material = goodInfo.material,
                        name = goodInfo.name,
                        units = repository.getUnitsByCode(goodInfo.unitsCode),
                        planned = goodInfo.quantity,
                        raws = taskInfoResult.raws.filter { it.material == goodInfo.material }.map { rawInfo ->
                            Raw(
                                    material = rawInfo.material,
                                    materialOsn = rawInfo.materialOsn,
                                    orderNumber = rawInfo.orderNumber,
                                    name = rawInfo.name,
                                    planned = rawInfo.planned
                            )
                        }.toMutableList(),
                        packs = taskInfoResult.packs.filter { it.material == goodInfo.material }.map { packInfo ->
                            Pack(
                                    material = packInfo.material,
                                    materialOsn = packInfo.materialOsn,
                                    code = packInfo.code,
                                    quantity = packInfo.quantity
                            )
                        }.toMutableList()
                )
            }

            currentTask.value = task
        }
    }

    override fun completeCurrentTask() {
        tasks.value?.let {
            it.find { task -> task.number == currentTask.value?.number }?.let { currentTask ->
                currentTask.isProcessed = true
            }

            tasks.value = it
        }
    }

    override fun completeCurrentGood() {
        currentTask.value?.let { task ->
            task.goods?.find { it.material == currentGood.value?.material }?.let { good ->
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
}

interface ITaskManager {
    var taskType: TaskType

    val tasks: MutableLiveData<List<Task>>
    val currentTask: MutableLiveData<Task>
    val currentGood: MutableLiveData<Good>
    val currentRaw: MutableLiveData<Raw>

    fun addTasks(taskListResult: TaskListResult)
    suspend fun addTaskInfoToCurrentTask(taskInfoResult: TaskInfoResult)
    fun getTaskTypeCode(): Int
    fun getBlockType(): Int
    fun completeCurrentTask()
    fun completeCurrentGood()
}