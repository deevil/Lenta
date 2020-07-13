package com.lenta.inventory.di

import android.content.Context
import app_update.AppUpdateInstaller
import app_update.AppUpdaterConfig
import app_update.AppUpdaterInstallerFromFmp
import com.google.gson.Gson
import com.lenta.inventory.BuildConfig.APPLICATION_ID
import com.lenta.inventory.models.IPersistInventoryTask
import com.lenta.inventory.models.PersistInventoryTask
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.InventoryTaskManager
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.platform.navigation.ScreenNavigator
import com.lenta.inventory.progress.IInventoryProgressUseCaseInformator
import com.lenta.inventory.progress.ProgressUseCaseInformator
import com.lenta.inventory.repos.IRepoInMemoryHolder
import com.lenta.inventory.repos.RepoInMemoryHolder
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.di.AppScope
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.progress.IProgressUseCaseInformator
import com.mobrun.plugin.api.HyperHive
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [AppModule.Declarations::class])
open class AppModule {

    @Module
    internal interface Declarations {
        @Binds
        @AppScope
        fun bindAppUpdateInstaller(realisation: AppUpdaterInstallerFromFmp): AppUpdateInstaller
    }

    @Provides
    @AppScope
    internal fun provideInventoryTaskManager(): IInventoryTaskManager {
        return InventoryTaskManager()
    }

    @Provides
    @AppScope
    internal fun provideProgressUseCaseInformator(
            progressUseCaseInformator: IProgressUseCaseInformator,
            context: Context): IInventoryProgressUseCaseInformator {
        return ProgressUseCaseInformator(progressUseCaseInformator, context)
    }

    @Provides
    @AppScope
    internal fun provideScreenNavigator(
            context: Context,
            iCoreNavigator: ICoreNavigator,
            foregroundActivityProvider: ForegroundActivityProvider,
            authenticator: IAuthenticator,
            progressUseCaseInformator: IInventoryProgressUseCaseInformator
    ): IScreenNavigator {
        return ScreenNavigator(context, iCoreNavigator, foregroundActivityProvider, authenticator, progressUseCaseInformator)
    }

    @Provides
    @AppScope
    internal fun provideIRepoInMemoryHolder(): IRepoInMemoryHolder {
        return RepoInMemoryHolder()
    }

    @Provides
    @AppScope
    internal fun provideIPersistInventoryTask(hyperHive: HyperHive, gson: Gson): IPersistInventoryTask {
        return PersistInventoryTask(hyperHive, gson)
    }

    @Provides
    @AppScope
    internal fun provideAppUpdaterConfig(): AppUpdaterConfig {
        return AppUpdaterConfig(folderName = "bp11", applicationId = APPLICATION_ID)
    }

}