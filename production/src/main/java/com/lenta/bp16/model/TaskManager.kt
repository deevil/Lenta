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
        taskListResult.tasks.forEach { processingUnit ->
            val existTask = taskList.find { it.taskInfo.number == processingUnit.number }
            if (existTask == null) {
                taskList.add(Task(
                        status = processingUnit.getTaskStatus(),
                        taskInfo = processingUnit,
                        type = taskType
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

}

interface ITaskManager {
    var taskType: TaskType
    val tasks: MutableLiveData<List<Task>>
    var currentTask: Task
    var currentGood: Good
    var currentRaw: Raw

    fun addTasks(taskListResult: TaskListResult)
    suspend fun addTaskInfoToCurrentTask(taskInfoResult: TaskInfoResult)
}