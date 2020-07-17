package com.lenta.bp18.di

import app_update.AppUpdateInstaller
import app_update.AppUpdaterConfig
import app_update.AppUpdaterInstallerFromFmp
import com.lenta.bp18.BuildConfig.APPLICATION_ID
import com.lenta.bp18.platform.Constants
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.platform.navigation.ScreenNavigator
import com.lenta.shared.di.AppScope
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

    }

    @Provides
    @AppScope
    internal fun provideAppUpdaterConfig(): AppUpdaterConfig {
        return AppUpdaterConfig(folderName = Constants.APP_FOLDER_NAME, applicationId = APPLICATION_ID)
    }

}