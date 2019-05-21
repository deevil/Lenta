package com.lenta.shared

import android.app.Application
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.CoreComponentProvider
import com.lenta.shared.di.CoreModule
import com.lenta.shared.di.DaggerCoreComponent
import com.lenta.shared.settings.DefaultConnectionSettings

abstract class CoreApplication : Application(), CoreComponentProvider {

    private lateinit var coreComponent: CoreComponent


    override fun provideCoreComponent(): CoreComponent {
        if (!this::coreComponent.isInitialized) {
            coreComponent = DaggerCoreComponent.builder()
                    .coreModule(CoreModule(this, getDefaultConnectionSettings()))
                    .build()
        }
        return coreComponent
    }

    abstract fun getDefaultConnectionSettings(): DefaultConnectionSettings


}