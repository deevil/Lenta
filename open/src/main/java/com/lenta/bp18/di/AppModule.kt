package com.lenta.bp18.di

import app_update.AppUpdateInstaller
import app_update.AppUpdaterConfig
import app_update.AppUpdaterInstallerFromFmp
import com.lenta.bp18.BuildConfig.APPLICATION_ID
import com.lenta.bp18.model.ITaskManager
import com.lenta.bp18.model.TaskManager
import com.lenta.bp18.platform.Constants
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.platform.navigation.ScreenNavigator
import com.lenta.bp18.repository.IRepoInMemoryHolder
import com.lenta.bp18.repository.RepoInMemoryHolder
import com.lenta.shared.di.AppScope
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

        @Binds
        @AppScope
        fun bindScreenNavigator(realisation: ScreenNavigator): IScreenNavigator

        @Binds
        @AppScope
        fun bindRepoInMemoryHolder(realisation: RepoInMemoryHolder): IRepoInMemoryHolder

        @Binds
        @AppScope
        fun bindTaskManager(realisation: TaskManager): ITaskManager

    }

    @Provides
    @AppScope
    internal fun provideAppUpdaterConfig(): AppUpdaterConfig {
        return AppUpdaterConfig(folderName = Constants.APP_FOLDER_NAME, applicationId = APPLICATION_ID)
    }

}