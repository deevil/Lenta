package com.lenta.bp14.models.check_list

import com.google.gson.Gson
import com.lenta.bp14.models.BaseTaskManager
import com.lenta.bp14.models.check_list.repo.CheckListRepo
import com.lenta.shared.di.AppScope
import com.lenta.shared.platform.time.ITimeMonitor
import javax.inject.Inject

@AppScope
class CheckListTaskManager @Inject constructor(
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : BaseTaskManager<ICheckListTask, CheckListTaskDescription>() {

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

