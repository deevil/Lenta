package com.lenta.bp10

import android.app.Application
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.CoreComponentProvider
import com.lenta.shared.di.CoreModule
import com.lenta.shared.di.DaggerCoreComponent

class AndroidApplication : Application(), CoreComponentProvider {

    private lateinit var coreComponent: CoreComponent


    override fun provideCoreComponent(): CoreComponent {
        if (!this::coreComponent.isInitialized) {
            coreComponent = DaggerCoreComponent.builder()
                    .coreModule(CoreModule(this))
                    .build()
        }
        return coreComponent
    }
}