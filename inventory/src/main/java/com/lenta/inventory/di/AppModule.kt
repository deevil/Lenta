package com.lenta.inventory.di

import android.content.Context
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
import dagger.Module
import dagger.Provides

@Module
class AppModule {


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
            progressUseCaseInformator: IProgressUseCaseInformator
    ): IScreenNavigator {
        return ScreenNavigator(context, iCoreNavigator, foregroundActivityProvider, authenticator, progressUseCaseInformator)
    }

    @Provides
    @AppScope
    internal fun provideIRepoInMemoryHolder(): IRepoInMemoryHolder {
        return RepoInMemoryHolder()
    }


}