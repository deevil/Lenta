package com.lenta.inventory.di

import android.content.Context
import com.lenta.bp10.features.auth.Authenticator
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.platform.navigation.ScreenNavigator
import com.lenta.inventory.exception.IInventoryFailureInterpretator
import com.lenta.inventory.exception.InventoryFailureInterpretator
import com.lenta.inventory.progress.ProgressUseCaseInformator
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.di.AppScope
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.progress.IProgressUseCaseInformator
import com.mobrun.plugin.api.HyperHive
import dagger.Module
import dagger.Provides

@Module
class AppModule {
    @Provides
    @AppScope
    internal fun provideAuthenticator(hyperHive: HyperHive): IAuthenticator {
        return Authenticator(hyperHive)
    }

    @Provides
    @AppScope
    internal fun provideFailureInterpreter(context: Context, coreFailureInterpreter: IFailureInterpreter): IInventoryFailureInterpretator {
        return InventoryFailureInterpretator(context, coreFailureInterpreter)
    }

    @Provides
    @AppScope
    internal fun provideProgressUseCaseInformator(context: Context): IProgressUseCaseInformator {
        return ProgressUseCaseInformator(context)
    }

    @Provides
    @AppScope
    internal fun provideScreenNavigator(
            context: Context,
            iCoreNavigator: ICoreNavigator,
            foregroundActivityProvider: ForegroundActivityProvider,
            authenticator: IAuthenticator,
            faultInterpreter: IInventoryFailureInterpretator,
            progressUseCaseInformator: IProgressUseCaseInformator
    ): IScreenNavigator {
        return ScreenNavigator(context, iCoreNavigator, foregroundActivityProvider, authenticator, faultInterpreter, progressUseCaseInformator)
    }


}