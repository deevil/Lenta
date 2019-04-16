package com.lenta.bp10.platform.extentions

import android.app.Activity
import com.lenta.bp10.di.AppComponent
import com.lenta.bp10.di.AppModule
import com.lenta.bp10.di.DaggerAppComponent
import com.lenta.shared.di.CoreInjectHelper

var appComponent: AppComponent? = null

fun Activity.getAppComponent(): AppComponent {
    if (appComponent == null) {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule())
                .coreComponent(CoreInjectHelper.provideCoreComponent(this.applicationContext)).build()
    }
    return appComponent!!
}