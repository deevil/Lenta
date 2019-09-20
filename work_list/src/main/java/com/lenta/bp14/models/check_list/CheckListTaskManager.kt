package com.lenta.bp14.models.check_list

import com.google.gson.Gson
import com.lenta.bp14.models.ITaskManager
import com.lenta.bp14.models.check_list.repo.CheckListRepo
import com.lenta.shared.platform.time.ITimeMonitor

class CheckListTaskManager(
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : ITaskManager<ICheckListTask, CheckListTaskDescription> {

    override var _task: ICheckListTask? = null

    override fun newTask(taskDescription: CheckListTaskDescription): ICheckListTask? {
        _task = CheckListTask(
                checkListRepo = CheckListRepo(),
                taskDescription = taskDescription,
                timeMonitor = timeMonitor,
                gson = gson
        )
        return _task
    }

}

