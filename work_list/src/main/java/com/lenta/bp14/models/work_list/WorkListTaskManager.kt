package com.lenta.bp14.models.work_list

import com.lenta.bp14.di.DaggerWorkListComponent
import com.lenta.bp14.di.WorkListComponent
import com.lenta.bp14.di.WorkListModule
import com.lenta.bp14.models.BaseTaskManager
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreInjectHelper
import javax.inject.Inject

@AppScope
class WorkListTaskManager @Inject constructor() : BaseTaskManager<IWorkListTask, WorkListTaskDescription>() {

    private val componentClazz = WorkListComponent::class.java

    override fun newTask(taskDescription: WorkListTaskDescription): IWorkListTask? {
        _task = CoreInjectHelper.createComponent(componentClazz) {
            DaggerWorkListComponent.builder()
                    .appComponent(getAppComponent(null))
                    .workListModule(WorkListModule(taskDescription))
                    .build()
        }.getTask()

        return _task
    }

    override fun getComponentClass(): Class<out Any> {
        return componentClazz
    }

}