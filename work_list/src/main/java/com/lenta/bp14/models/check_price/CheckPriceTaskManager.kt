package com.lenta.bp14.models.check_price

import com.lenta.bp14.di.CheckPriceComponent
import com.lenta.bp14.di.CheckPriceModule
import com.lenta.bp14.di.DaggerCheckPriceComponent
import com.lenta.bp14.models.BaseTaskManager
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreInjectHelper
import javax.inject.Inject

@AppScope
class CheckPriceTaskManager @Inject constructor() : BaseTaskManager<ICheckPriceTask, CheckPriceTaskDescription>() {
    private val componentClazz = CheckPriceComponent::class.java

    override fun newTask(taskDescription: CheckPriceTaskDescription): ICheckPriceTask? {
        _task = CoreInjectHelper.createComponent(componentClazz) {
            DaggerCheckPriceComponent.builder()
                    .appComponent(getAppComponent(null))
                    .checkPriceModule(CheckPriceModule(taskDescription))
                    .build()
        }.getTask()

        return _task
    }

    override fun getComponentClass(): Class<out Any> {
        return Any::class.java
    }
}