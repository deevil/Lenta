package com.lenta.bp10.di

import android.content.Context
import android.os.Handler
import com.google.gson.GsonBuilder
import com.lenta.bp10.BuildConfig
import com.lenta.shared.di.AppScope
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.HyperHiveState
import com.mobrun.plugin.api.VersionAPI
import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    @AppScope
    internal fun provideHyperHiveState(appContext: Context): HyperHiveState {
        return HyperHiveState(appContext)
                .setHostWithSchema("http://9.6.24.110")
                .setApiVersion(VersionAPI.V_1)
                .setEnvironmentSlug("Lenta_LRQ")
                .setProjectSlug("PR_WOB")
                .setVersionProject("app")
                .setHandler(Handler())
                .setDefaultRetryCount(5)
                .setDefaultRetryIntervalSec(10)
                .setGsonForParcelPacker(GsonBuilder().excludeFieldsWithoutExposeAnnotation().create())
    }


    @Provides
    @AppScope
    internal fun provideHyperHive(hyperHiveState: HyperHiveState): HyperHive {
        val hyperHive = hyperHiveState
                .buildHyperHive()

        if (BuildConfig.DEBUG) {
            //Easy.logD("hhive plugin version: " + hyperHive.stateAPI.versionPlugin)
            //Easy.logD("hhive core version: " + hyperHive.stateAPI.getVersionCoreAPI(0))
            hyperHive.loggingAPI.setLogLevel(0)
        } else {
            hyperHive.loggingAPI.setLogLevel(10)
        }

        return hyperHive

    }
}