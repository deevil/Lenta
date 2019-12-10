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

    override lateinit var currentTask: Task

    override lateinit var currentGood: Good

    override lateinit var currentRaw: Raw

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
        currentTask.apply {
            goods = taskInfoResult.goods.map { goodInfo ->
                Good(
                        material = goodInfo.material,
                        name = goodInfo.name,
                        units = repository.getUnitsByCode(goodInfo.unitsCode),
                        planned = goodInfo.quantity,
                        raws = taskInfoResult.raws.filter { it.material == goodInfo.material }.map { rawInfo ->
                            Raw(
                                    orderNumber = rawInfo.orderNumber,
                                    materialOsn = rawInfo.materialOsn,
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
        }
    }

    override fun completeCurrentTask() {
        val taskList = tasks.value!!.toMutableList()
        taskList.find { it.number == currentTask.number }?.let { currentTask ->
            currentTask.isProcessed = true
        }

        tasks.value = taskList
    }

    override fun getTaskType(): Int {
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
    var currentTask: Task
    var currentGood: Good
    var currentRaw: Raw

    fun addTasks(taskListResult: TaskListResult)
    suspend fun addTaskInfoToCurrentTask(taskInfoResult: TaskInfoResult)
    fun getTaskType(): Int
    fun getBlockType(): Int
    fun completeCurrentTask()
}