package com.lenta.bp14.models.check_list

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskManager
import com.lenta.bp14.models.check_price.ICheckPriceResult
import com.lenta.shared.platform.time.ITimeMonitor


class CheckListTaskManager(private val timeMonitor: ITimeMonitor, private val gson: Gson) : ITaskManager<ICheckListTask, CheckListTaskDescription> {

    private var checkListTask: ICheckListTask? = null

    override fun getTask(): ICheckListTask? {
        return checkListTask
    }

    override fun newTask(taskDescription: CheckListTaskDescription?): ICheckListTask? {
        checkListTask = CheckL
        return checkListTask
    }

    override fun clearTask() {
        checkListTask = null
    }

    override fun setTask(inventoryTask: ICheckListTask?) {
        checkListTask = inventoryTask
    }

}

