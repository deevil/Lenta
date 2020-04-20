package com.lenta.movement.di

import android.content.Context
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.platform.navigation.ScreenNavigator
import com.lenta.movement.repos.IRepoInMemoryHolder
import com.lenta.movement.repos.RepoInMemoryHolder
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.di.AppScope
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
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
        authenticator: IAuthenticator
    ): IScreenNavigator {
        return ScreenNavigator(context, iCoreNavigator, foregroundActivityProvider, authenticator)
    }

    @Provides
    @AppScope
    internal fun provideIRepoInMemoryHolder(): IRepoInMemoryHolder {
        return RepoInMemoryHolder()
    }

}