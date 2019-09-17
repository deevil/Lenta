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
) : ITaskManager<IWorkListTask, WorkListTaskDescription> {

    private var workListTask: IWorkListTask? = null

    override fun getTask(): IWorkListTask? {
        return workListTask
    }

    override fun newTask(taskDescription: WorkListTaskDescription): IWorkListTask? {
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

    override fun setTask(inventoryTask: IWorkListTask?) {
        workListTask = inventoryTask
    }

    override fun getCurrentTaskType(): ITaskType? {
        return if (getTask() != null) TaskTypes.WorkList.taskType else null
    }

}