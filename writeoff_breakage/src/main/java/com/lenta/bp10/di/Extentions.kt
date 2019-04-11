package com.lenta.bp10.di

import android.app.Activity
import com.lenta.shared.di.CoreInjectHelper

fun Activity.getAppComponent(): AppComponent {
    return DaggerAppComponent
            .builder()
            .coreComponent(CoreInjectHelper.provideCoreComponent(this.applicationContext)).build()
}