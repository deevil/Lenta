package com.lenta.movement.platform.extensions

import com.lenta.movement.di.AppComponent
import com.lenta.movement.di.AppModule
import com.lenta.movement.di.DaggerAppComponent
import com.lenta.shared.di.CoreComponent

var appComponent: AppComponent? = null

fun getAppComponent(coreComponent: CoreComponent): AppComponent {
    if (appComponent == null) {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule())
                .coreComponent(coreComponent).build()
    }
    return appComponent!!
}