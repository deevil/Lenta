package com.lenta.bp12.platform.extention

import com.lenta.bp12.di.AppComponent
import com.lenta.bp12.di.AppModule
import com.lenta.bp12.di.DaggerAppComponent
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.CoreInjectHelper.createComponent
import com.lenta.shared.di.CoreInjectHelper.getComponent

fun getAppComponent(coreComponent: CoreComponent?): AppComponent {
    AppComponent::class.java.let { clazz ->
        getComponent(clazz)?.let {
            return it
        }
        return createComponent(clazz) {
            DaggerAppComponent.builder()
                    .appModule(AppModule())
                    .coreComponent(coreComponent).build()
        }
    }
}
