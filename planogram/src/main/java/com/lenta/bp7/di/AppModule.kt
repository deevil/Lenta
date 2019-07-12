package com.lenta.bp7.di

import android.content.Context
import com.lenta.bp7.account.PlanogramSessionInfo
import com.lenta.bp7.account.IPlanogramSessionInfo
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.platform.navigation.ScreenNavigator
import com.lenta.bp7.repos.DatabaseRepo
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.bp7.repos.IRepoInMemoryHolder
import com.lenta.bp7.repos.RepoInMemoryHolder
import com.lenta.bp7.requests.network.SlowResourcesMultiRequest
import com.lenta.bp7.requests.network.loader.ResourcesLoader
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.di.AppScope
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

    @Provides
    @AppScope
    internal fun provideResourceLoader(slowResourcesNetRequest: SlowResourcesMultiRequest): ResourcesLoader {
        return ResourcesLoader(slowResourcesNetRequest)
    }

    @Provides
    @AppScope
    internal fun provideIGoodInformationRepo(hyperHive: HyperHive): IDatabaseRepo {
        return DatabaseRepo(hyperHive)
    }

    @Provides
    @AppScope
    internal fun provideIPlanogramSessionInfo(): IPlanogramSessionInfo {
        return PlanogramSessionInfo()
    }
}