package com.lenta.bp10.di

import android.content.Context
import app_update.AppUpdateInstaller
import app_update.AppUpdaterConfig
import app_update.AppUpdaterInstallerFromFmp
import com.google.gson.Gson
import com.lenta.bp10.BuildConfig.APPLICATION_ID
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
import com.lenta.bp10.repos.IRepoInMemoryHolder
import com.lenta.bp10.repos.RepoInMemoryHolder
import com.lenta.bp10.requests.network.SlowResourcesMultiRequest
import com.lenta.bp10.requests.network.loader.ResourcesLoader
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.di.AppScope
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.progress.IProgressUseCaseInformator
import com.mobrun.plugin.api.HyperHive
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [AppModule.Declarations::class])
class AppModule {

    @Module
    internal interface Declarations {
        @Binds
        @AppScope
        fun bindAppUpdateInstaller(realisation: AppUpdaterInstallerFromFmp): AppUpdateInstaller
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
    internal fun provideIGoodInformationRepo(hyperHive: HyperHive, repoInMemoryHolder: IRepoInMemoryHolder): IGoodInformationRepo {
        return GoodInformationRepo(hyperHive, repoInMemoryHolder)
    }

    @Provides
    @AppScope
    internal fun provideIRepoInMemoryHolder(): IRepoInMemoryHolder {
        return RepoInMemoryHolder()
    }

    @Provides
    @AppScope
    internal fun provideAppUpdaterConfig(): AppUpdaterConfig {
        return AppUpdaterConfig(folderName = "bp10", applicationId = APPLICATION_ID)
    }


}