package com.lenta.bp10.platform.extentions

import com.lenta.inventory.di.AppComponent
import com.lenta.inventory.di.AppModule
import com.lenta.inventory.di.DaggerAppComponent
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