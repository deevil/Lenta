package com.lenta.bp7.platform.extentions

import com.lenta.bp7.di.AppComponent
import com.lenta.bp7.di.AppModule
import com.lenta.bp7.di.DaggerAppComponent
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