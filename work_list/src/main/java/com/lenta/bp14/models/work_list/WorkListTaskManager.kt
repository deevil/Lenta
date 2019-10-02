package com.lenta.bp14.models.work_list

import com.google.gson.Gson
import com.lenta.bp14.models.BaseTaskManager
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.work_list.repo.WorkListRepo
import com.lenta.shared.di.AppScope
import com.lenta.shared.platform.time.ITimeMonitor
import javax.inject.Inject

@AppScope
class WorkListTaskManager @Inject constructor(
        private val generalRepo: IGeneralRepo,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : BaseTaskManager<WorkListTask, WorkListTaskDescription>() {

    override fun newTask(taskDescription: WorkListTaskDescription): WorkListTask? {
        _task = WorkListTask(
                generalRepo = generalRepo,
                workListRepo = WorkListRepo(),
                taskDescription = taskDescription,
                timeMonitor = timeMonitor,
                gson = gson
        )
        return _task
    }

    override fun getComponentClass(): Class<out Any> {
        return Any::class.java
    }

}