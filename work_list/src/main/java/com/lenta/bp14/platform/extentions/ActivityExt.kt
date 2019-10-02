package com.lenta.bp14.platform.extentions

import com.lenta.bp14.di.*
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

