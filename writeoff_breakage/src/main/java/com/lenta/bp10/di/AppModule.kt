package com.lenta.bp10.di

import android.content.Context
import com.google.gson.Gson
import com.lenta.bp10.features.auth.Authenticator
import com.lenta.bp10.features.good_information.GoodInformationRepo
import com.lenta.bp10.features.good_information.IGoodInformationRepo
import com.lenta.bp10.features.job_card.IJobCardRepo
import com.lenta.bp10.features.job_card.JobCardRepo
import com.lenta.bp10.models.IPersistWriteOffTask
import com.lenta.bp10.models.PersistWriteOffTask
import com.lenta.bp10.models.WriteOffTaskManager
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.platform.navigation.ScreenNavigator
import com.lenta.bp10.platform.resources.IStringResourceManager
import com.lenta.bp10.platform.resources.StringResourceManager
import com.lenta.bp10.progress.IWriteOffProgressUseCaseInformator
import com.lenta.bp10.progress.ProgressUseCaseInformator
import com.lenta.bp10.requests.network.SlowResourcesMultiRequest
import com.lenta.bp10.requests.network.loader.ResourcesLoader
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.di.AppScope
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.progress.IProgressUseCaseInformator
import com.mobrun.plugin.api.HyperHive
import dagger.Module
import dagger.Provides

@Module
class AppModule {


    @Provides
    @AppScope
    internal fun provideAuthenticator(hyperHive: HyperHive): IAuthenticator {
        return Authenticator(hyperHive)
    }

    @Provides
    @AppScope
    internal fun provideScreenNavigator(
            context: Context,
            iCoreNavigator: ICoreNavigator,
            foregroundActivityProvider: ForegroundActivityProvider,
            authenticator: IAuthenticator,
            progressUseCaseInformator: IWriteOffProgressUseCaseInformator
    ): IScreenNavigator {
        return ScreenNavigator(context, iCoreNavigator, foregroundActivityProvider, authenticator, progressUseCaseInformator)
    }



    @Provides
    @AppScope
    internal fun provideResourceLoader(slowResourcesNetRequest: SlowResourcesMultiRequest): ResourcesLoader {
        return ResourcesLoader(slowResourcesNetRequest)
    }

    @Provides
    @AppScope
    internal fun provideProgressUseCaseInformator(coreProgressUseCaseInformator: IProgressUseCaseInformator, context: Context): IWriteOffProgressUseCaseInformator {
        return ProgressUseCaseInformator(coreProgressUseCaseInformator, context)
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
    internal fun provideIPersistWriteOffTask(hyperHive: HyperHive, gson: Gson): IPersistWriteOffTask {
        return PersistWriteOffTask(hyperHive, gson)
    }

    @Provides
    @AppScope
    internal fun provideSharedStringResourceManager(context: Context, resourceManager: ISharedStringResourceManager): IStringResourceManager {
        return StringResourceManager(context, resourceManager)
    }

    @Provides
    @AppScope
    internal fun provideIGoodInformationRepo(hyperHive: HyperHive): IGoodInformationRepo {
        return GoodInformationRepo(hyperHive)
    }


}