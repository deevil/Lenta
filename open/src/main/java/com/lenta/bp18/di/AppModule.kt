package com.lenta.bp18.di

import android.content.Context
import app_update.AppUpdateInstaller
import app_update.AppUpdaterConfig
import app_update.AppUpdaterInstallerFromFmp
import com.lenta.bp18.BuildConfig.APPLICATION_ID
import com.lenta.bp18.platform.Constants
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.platform.navigation.ScreenNavigator
import com.lenta.bp18.repository.DatabaseRepo
import com.lenta.bp18.repository.IDatabaseRepo
import com.lenta.bp18.repository.IRepoInMemoryHolder
import com.lenta.bp18.repository.RepoInMemoryHolder
import com.lenta.bp18.request.SlowResourcesMultiRequest
import com.lenta.bp18.request.loader.ResourcesLoader
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
class AppModule {

    @Module
    internal interface Declarations {

        @Binds
        @AppScope
        fun bindAppUpdateInstaller(realisation: AppUpdaterInstallerFromFmp): AppUpdateInstaller

    }

    @Provides
    @AppScope
    internal fun provideScreenNavigator(context: Context,
                                        iCoreNavigator: ICoreNavigator,
                                        foregroundActivityProvider: ForegroundActivityProvider,
                                        authenticator: IAuthenticator,
                                        progressUseCaseInformator: IProgressUseCaseInformator): IScreenNavigator {
        return ScreenNavigator(context, iCoreNavigator, foregroundActivityProvider, authenticator, progressUseCaseInformator)
    }

    @Provides
    @AppScope
    internal fun provideIRepoInMemoryHolder(): IRepoInMemoryHolder {
        return RepoInMemoryHolder()
    }

    @Provides
    @AppScope
    internal fun provideIGoodInformationRepo(hyperHive: HyperHive): IDatabaseRepo {
        return DatabaseRepo(hyperHive)
    }

    @Provides
    @AppScope
    internal fun provideAppUpdaterConfig(): AppUpdaterConfig {
        return AppUpdaterConfig(folderName = Constants.APP_FOLDER_NAME, applicationId = APPLICATION_ID)
    }

}