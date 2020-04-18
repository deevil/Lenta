package com.lenta.movement.di

import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.platform.navigation.ScreenNavigator
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
        iCoreNavigator: ICoreNavigator,
        foregroundActivityProvider: ForegroundActivityProvider,
        authenticator: IAuthenticator
    ): IScreenNavigator {
        return ScreenNavigator(iCoreNavigator, foregroundActivityProvider, authenticator)
    }

}