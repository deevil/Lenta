package com.lenta.bp10.di

import android.content.Context
import android.os.Handler
import com.google.gson.GsonBuilder
import com.lenta.bp10.BuildConfig
import com.lenta.bp10.features.auth.Authenticator
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.platform.navigation.ScreenNavigator
import com.lenta.bp10.requests.network.SlowResourcesMultiRequest
import com.lenta.bp10.requests.network.loader.ResourcesLoader
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.di.AppScope
import com.lenta.shared.exception.FailureInterpreter
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.IGoBackNavigator
import com.lenta.shared.utilities.Logg
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
            Logg.d { "hhive plugin version: ${hyperHive.stateAPI.versionPlugin}" }
            Logg.d { "hhive core version: ${hyperHive.stateAPI.getVersionCoreAPI(0)}" }
            hyperHive.loggingAPI.setLogLevel(3)
        } else {
            hyperHive.loggingAPI.setLogLevel(10)
        }

        return hyperHive

    }

    @Provides
    @AppScope
    internal fun provideAuthenticator(hyperHive: HyperHive): IAuthenticator {
        return Authenticator(hyperHive)
    }

    @Provides
    @AppScope
    internal fun provideScreenNavigator(foregroundActivityProvider: ForegroundActivityProvider,
                                        authenticator: IAuthenticator
    ): IScreenNavigator {
        return ScreenNavigator(foregroundActivityProvider, authenticator)
    }

    @Provides
    @AppScope
    internal fun provideGoBackNavigator(screenNavigator: IScreenNavigator): IGoBackNavigator {
        return screenNavigator
    }

    @Provides
    @AppScope
    internal fun provideFailureInterpreter(context: Context): IFailureInterpreter {
        return FailureInterpreter(context)
    }

    @Provides
    @AppScope
    internal fun provideResourceLoader(slowResourcesNetRequest: SlowResourcesMultiRequest): ResourcesLoader {
        return ResourcesLoader(slowResourcesNetRequest)
    }

}