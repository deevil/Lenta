package com.lenta.bp14.models.check_list

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.shared.platform.time.ITimeMonitor

class CheckListTask(
        private val taskDescription: CheckListTaskDescription,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : ICheckListTask {

    override fun getCheckResults(): LiveData<List<ICheckListResult>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTaskType(): ITaskType {
        return TaskTypes.CheckPrice.taskType
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

}

interface ICheckListTask : ITask {
    fun getCheckResults(): LiveData<List<ICheckListResult>>
}

data class CheckListResult(
        override val ean: String,
        override val matNr: String?,
        override val time: Long
) : ICheckListResult {



}

interface ICheckListResult {
    val ean: String
    val matNr: String?
    val time: Long
}



