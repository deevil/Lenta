package com.lenta.bp14.models.work_list

import com.google.gson.Gson
import com.lenta.bp14.models.ITaskManager
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.bp14.models.work_list.repo.WorkListRepo
import com.lenta.shared.platform.time.ITimeMonitor

class WorkListTaskManager(
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : ITaskManager<WorkListTask, WorkListTaskDescription> {

    private var workListTask: WorkListTask? = null

    override fun getTask(): WorkListTask? {
        return workListTask
    }

    override fun newTask(taskDescription: WorkListTaskDescription): WorkListTask? {
        workListTask = WorkListTask(
                workListRepo = WorkListRepo(),
                taskDescription = taskDescription,
                timeMonitor = timeMonitor,
                gson = gson
        )
        return workListTask
    }

    override fun clearTask(): Boolean {
        if (workListTask == null) {
            return false
        }
        workListTask = null
        return true
    }

    override fun setTask(inventoryTask: WorkListTask?) {
        workListTask = inventoryTask
    }

    override fun getCurrentTaskType(): ITaskType? {
        return if (getTask() != null) TaskTypes.WorkList.taskType else null
    }

}