package com.lenta.bp14.models.not_exposed

import com.lenta.bp14.di.DaggerNotExposedComponent
import com.lenta.bp14.di.NotExposedComponent
import com.lenta.bp14.di.NotExposedModule
import com.lenta.bp14.models.BaseTaskManager
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreInjectHelper
import javax.inject.Inject

@AppScope
class NotExposedTaskManager @Inject constructor() : BaseTaskManager<INotExposedTask, NotExposedTaskDescription>() {

    private val componentClazz = NotExposedComponent::class.java

    override fun newTask(taskDescription: NotExposedTaskDescription): INotExposedTask? {
        _task = CoreInjectHelper.createComponent(componentClazz) {
            DaggerNotExposedComponent.builder()
                    .appComponent(getAppComponent(null))
                    .notExposedModule(NotExposedModule(taskDescription))
                    .build()
        }.getNotExposedProductsTask()
        return _task
    }

    override fun getComponentClass(): Class<out Any> {
        return componentClazz
    }
}