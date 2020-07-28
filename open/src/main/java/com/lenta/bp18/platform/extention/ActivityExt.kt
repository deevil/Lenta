package com.lenta.bp18.platform.extention

import com.lenta.bp18.di.AppComponent
import com.lenta.bp18.di.AppModule
import com.lenta.bp18.di.DaggerAppComponent
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.CoreInjectHelper.createComponent
import com.lenta.shared.di.CoreInjectHelper.getComponent


var appComponent: AppComponent? = null

fun getAppComponent(coreComponent: CoreComponent): AppComponent {
    if (appComponent == null) {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule())
                .coreComponent(coreComponent).build()
    }
    return appComponent!!
}

