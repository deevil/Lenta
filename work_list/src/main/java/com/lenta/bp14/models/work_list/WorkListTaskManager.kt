package com.lenta.bp14.models.work_list

import com.google.gson.Gson
import com.lenta.bp14.models.BaseTaskManager
import com.lenta.bp14.models.work_list.repo.WorkListRepo
import com.lenta.shared.platform.time.ITimeMonitor

class WorkListTaskManager(
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : BaseTaskManager<WorkListTask, WorkListTaskDescription>() {

    override fun newTask(taskDescription: WorkListTaskDescription): WorkListTask? {
        _task = WorkListTask(
                workListRepo = WorkListRepo(),
                taskDescription = taskDescription,
                timeMonitor = timeMonitor,
                gson = gson
        )
        return _task
    }
}