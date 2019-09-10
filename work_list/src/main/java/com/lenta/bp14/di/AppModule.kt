package com.lenta.bp14.di

import android.content.Context
import com.google.gson.Gson
import com.lenta.bp14.models.data.TaskManager
import com.lenta.bp14.models.check_price.CheckPriceTaskManager
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.platform.navigation.ScreenNavigator
import com.lenta.bp14.repos.IRepoInMemoryHolder
import com.lenta.bp14.repos.RepoInMemoryHolder
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.di.AppScope
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.progress.IProgressUseCaseInformator
import dagger.Module
import dagger.Provides

@Module
class AppModule {


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
    internal fun provideTaskManager(): TaskManager {
        return TaskManager()
    }

    @Provides
    @AppScope
    internal fun provideCheckTaskManager(timeMonitor: ITimeMonitor, gson: Gson): CheckPriceTaskManager {
        return CheckPriceTaskManager(timeMonitor = timeMonitor, gson = gson)
    }

}