package com.lenta.shared

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.CoreComponentProvider
import com.lenta.shared.di.CoreModule
import com.lenta.shared.di.DaggerCoreComponent
import com.lenta.shared.settings.DefaultConnectionSettings
import com.lenta.shared.utilities.runIfRelease
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

abstract class CoreApplication : Application(), CoreComponentProvider {

    protected lateinit var coreComponent: CoreComponent

    override fun provideCoreComponent(): CoreComponent {
        if (!this::coreComponent.isInitialized) {
            coreComponent = DaggerCoreComponent.builder()
                    .coreModule(CoreModule(this, getDefaultConnectionSettings()))
                    .build().apply {
                        setDefaultUncaughtExceptionHandler(this)
                    }
        }
        return coreComponent
    }

    abstract fun getDefaultConnectionSettings(): DefaultConnectionSettings

    override fun onCreate() {
        super.onCreate()
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    private fun setDefaultUncaughtExceptionHandler(coreComponent: CoreComponent) {

        runIfRelease {
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                coreComponent.getIAnalytics().apply {
                    logFatal(tag = "UncaughtAndroidException",
                            message = "${printableString(throwable)}\n" +
                                    "IP: ${coreComponent.getINetworkStateMonitor().networkInfo.value?.ip}\n" +
                                    "Thread:${thread.name}")
                    sendLogs()
                }
                Thread {
                    onHandleException()
                    Thread.sleep(200)
                    exitProcess(1)
                }.run()

            }
        }

    }

    abstract fun onHandleException()


    private fun printableString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }

}