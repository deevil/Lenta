package com.lenta.bp9.di

import android.content.Context
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.ReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.platform.navigation.ScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.repos.RepoInMemoryHolder
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.di.AppScope
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
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
    internal fun provideReceivingTaskManager(): IReceivingTaskManager {
        return ReceivingTaskManager()
    }

}