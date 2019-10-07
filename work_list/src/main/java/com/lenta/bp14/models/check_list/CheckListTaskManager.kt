package com.lenta.bp14.models.check_list

import com.lenta.bp14.di.CheckListComponent
import com.lenta.bp14.di.CheckListModule
import com.lenta.bp14.di.DaggerCheckListComponent
import com.lenta.bp14.models.BaseTaskManager
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreInjectHelper
import javax.inject.Inject

@AppScope
class CheckListTaskManager @Inject constructor() : BaseTaskManager<ICheckListTask, CheckListTaskDescription>() {

    private val componentClazz = CheckListComponent::class.java

    override fun newTask(taskDescription: CheckListTaskDescription): ICheckListTask? {
        _task = CoreInjectHelper.createComponent(componentClazz) {
            DaggerCheckListComponent.builder()
                    .appComponent(getAppComponent(null))
                    .checkListModule(CheckListModule(taskDescription))
                    .build()
        }.getTask()

        return _task
    }

    override fun getComponentClass(): Class<out Any> {
        return componentClazz
    }

}

