package com.lenta.bp14.models.work_list

import com.google.gson.Gson
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.check_list.Good
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.bp14.models.work_list.repo.IWorkListRepo
import com.lenta.shared.platform.time.ITimeMonitor

class WorkListTask(
        private val workListRepo: IWorkListRepo,
        private val taskDescription: WorkListTaskDescription,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : IWorkListTask {

    override fun getGoodByEan(ean: String): Good? {
        return null
    }

    override fun getTaskType(): ITaskType {
        return TaskTypes.CheckPrice.taskType
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

}

interface IWorkListTask : ITask {
    fun getGoodByEan(ean: String): Good?
}