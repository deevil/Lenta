package com.lenta.bp10.di

import android.content.Context
import android.os.Handler
import com.google.gson.GsonBuilder
import com.lenta.bp10.BuildConfig
import com.lenta.bp10.account.SessionInfo
import com.lenta.bp10.features.auth.Authenticator
import com.lenta.bp10.features.job_card.IJobCardRepo
import com.lenta.bp10.features.job_card.JobCardRepo
import com.lenta.bp10.models.WriteOffTaskManager
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.platform.navigation.ScreenNavigator
import com.lenta.bp10.progress.ProgressUseCaseInformator
import com.lenta.bp10.requests.network.SlowResourcesMultiRequest
import com.lenta.bp10.requests.network.loader.ResourcesLoader
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.di.AppScope
import com.lenta.shared.exception.FailureInterpreter
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.IGoBackNavigator
import com.lenta.shared.progress.IProgressUseCaseInformator
import com.lenta.shared.scan.IScanHelper
import com.lenta.shared.scan.mobilbase.MobilBaseScanHelper
import com.lenta.shared.settings.IAppSettings
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
    internal fun provideHyperHiveState(appContext: Context, appSettings: IAppSettings): HyperHiveState {
        return HyperHiveState(appContext)
                .setHostWithSchema(appSettings.serverAddress)
                .setApiVersion(VersionAPI.V_1)
                .setEnvironmentSlug(appSettings.environment)
                .setProjectSlug(appSettings.project)
                .setVersionProject("app")
                .setHandler(Handler())
                .setDefaultRetryCount(6)
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
    internal fun provideScreenNavigator(
            context: Context,
            foregroundActivityProvider: ForegroundActivityProvider,
            authenticator: IAuthenticator,
            faultInterpreter: IFailureInterpreter,
            progressUseCaseInformator: IProgressUseCaseInformator
    ): IScreenNavigator {
        return ScreenNavigator(context, foregroundActivityProvider, authenticator, faultInterpreter, progressUseCaseInformator)
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

    @Provides
    @AppScope
    internal fun provideProgressUseCaseInformator(context: Context): IProgressUseCaseInformator {
        return ProgressUseCaseInformator(context)
    }

    @Provides
    @AppScope
    internal fun provideSessionInfo(): SessionInfo {
        return SessionInfo()
    }

    @Provides
    @AppScope
    internal fun provideISessionInfo(sessionInfo: SessionInfo): ISessionInfo {
        return sessionInfo
    }

    @Provides
    internal fun provideIJobCardRepo(jobCardRepo: JobCardRepo): IJobCardRepo {
        return jobCardRepo
    }

    @Provides
    @AppScope
    internal fun provideProcessProductServiceManager(): IWriteOffTaskManager {
        return WriteOffTaskManager()
    }

    @Provides
    @AppScope
    internal fun provideScanHelper(): IScanHelper {
        return MobilBaseScanHelper()
    }


}