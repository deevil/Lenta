package com.lenta.bp9.di

import android.content.Context
import app_update.AppUpdateInstaller
import app_update.AppUpdaterConfig
import app_update.AppUpdaterInstallerFromFmp
import com.lenta.bp9.BuildConfig.APPLICATION_ID
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.ReceivingTaskManager
import com.lenta.bp9.data.IPrinter
import com.lenta.bp9.data.Printer
import com.lenta.bp9.features.delegates.ISaveProductDelegate
import com.lenta.bp9.features.delegates.SaveProductDelegate
import com.lenta.bp9.model.memory.MemoryTaskRepository
import com.lenta.bp9.model.repositories.ITaskRepository
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.platform.navigation.ScreenNavigator
import com.lenta.bp9.repos.DataBaseRepo
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.repos.RepoInMemoryHolder
import com.lenta.bp9.requests.network.EndRecountDirectDeliveriesNetRequest
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.di.AppScope
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.progress.IProgressUseCaseInformator
import com.mobrun.plugin.api.HyperHive
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [AppModule.Declarations::class])
class AppModule {

    @Module
    internal interface Declarations {
        @Binds
        @AppScope
        fun bindAppUpdateInstaller(realisation: AppUpdaterInstallerFromFmp): AppUpdateInstaller

        @Binds
        @AppScope
        fun bindPrinter(realisation: Printer): IPrinter
    }

    @Provides
    @AppScope
    internal fun provideSaveProductDelegate(
            taskManager: IReceivingTaskManager,
            screenNavigator: IScreenNavigator,
            sessionInfo: ISessionInfo,
            context: Context,
            endRecountDirectDeliveries: EndRecountDirectDeliveriesNetRequest
    ): ISaveProductDelegate {
        return SaveProductDelegate(taskManager, screenNavigator, sessionInfo, context, endRecountDirectDeliveries)
    }

    @Provides
    @AppScope
    internal fun provideScreenNavigator(
            context: Context,
            iCoreNavigator: ICoreNavigator,
            foregroundActivityProvider: ForegroundActivityProvider,
            authenticator: IAuthenticator,
            progressUseCaseInformator: IProgressUseCaseInformator
    ): IScreenNavigator {
        return ScreenNavigator(context, iCoreNavigator, foregroundActivityProvider, authenticator, progressUseCaseInformator)
    }


    @Provides
    @AppScope
    internal fun provideRepoInMEmoryHolder(): IRepoInMemoryHolder {
        return RepoInMemoryHolder()
    }

    @Provides
    @AppScope
    internal fun provideReceivingTaskManager(memoryTaskRepository: ITaskRepository): IReceivingTaskManager {
        return ReceivingTaskManager(memoryTaskRepository)
    }

    @Provides
    @AppScope
    internal fun provideIDataBaseRepo(hyperHive: HyperHive): IDataBaseRepo {
        return DataBaseRepo(hyperHive)
    }

    @Provides
    @AppScope
    internal fun provideAppUpdaterConfig(): AppUpdaterConfig {
        return AppUpdaterConfig(folderName = "bp9", applicationId = APPLICATION_ID)
    }

    @Provides
    @AppScope
    internal fun provideMemoryTaskRepository(): ITaskRepository {
        return MemoryTaskRepository()
    }

}