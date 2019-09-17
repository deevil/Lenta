package com.lenta.bp14.models.check_list

import com.google.gson.Gson
import com.lenta.bp14.models.ITaskManager
import com.lenta.bp14.models.check_list.repo.CheckListRepo
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.shared.platform.time.ITimeMonitor

class CheckListTaskManager(
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : ITaskManager<ICheckListTask, CheckListTaskDescription> {

    private var checkListTask: ICheckListTask? = null

    override fun getTask(): ICheckListTask? {
        return checkListTask
    }

    override fun newTask(taskDescription: CheckListTaskDescription): ICheckListTask? {
        checkListTask = CheckListTask(
                checkListRepo = CheckListRepo(),
                taskDescription = taskDescription,
                timeMonitor = timeMonitor,
                gson = gson
        )
        return checkListTask
    }

    override fun clearTask(): Boolean {
        if (checkListTask == null) {
            return false
        }
        checkListTask = null
        return true
    }

    override fun setTask(inventoryTask: ICheckListTask?) {
        checkListTask = inventoryTask
    }

    override fun getCurrentTaskType(): ITaskType? {
        return if (getTask() != null) TaskTypes.CheckList.taskType else null
    }

}

