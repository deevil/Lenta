package com.lenta.movement.di

import android.content.Context
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.TaskManager
import com.lenta.movement.models.memory.MemoryBoxesRepository
import com.lenta.movement.models.memory.MemoryCargoUnitRepository
import com.lenta.movement.models.memory.MemoryTaskBasketsRepository
import com.lenta.movement.models.repositories.IBoxesRepository
import com.lenta.movement.models.repositories.ICargoUnitRepository
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.Formatter
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.platform.navigation.ScreenNavigator
import com.lenta.movement.progress.IWriteOffProgressUseCaseInformator
import com.lenta.movement.progress.ProgressUseCaseInformator
import com.lenta.movement.repos.IRepoInMemoryHolder
import com.lenta.movement.repos.RepoInMemoryHolder
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
    internal fun provideProgressUseCaseInformator(coreProgressUseCaseInformator: IProgressUseCaseInformator, context: Context): IWriteOffProgressUseCaseInformator {
        return ProgressUseCaseInformator(context, coreProgressUseCaseInformator)
    }

    @Provides
    @AppScope
    internal fun provideScreenNavigator(
        context: Context,
        iCoreNavigator: ICoreNavigator,
        foregroundActivityProvider: ForegroundActivityProvider,
        authenticator: IAuthenticator,
        progressUseCaseInformator: IWriteOffProgressUseCaseInformator
    ): IScreenNavigator {
        return ScreenNavigator(context, iCoreNavigator, foregroundActivityProvider, authenticator, progressUseCaseInformator)
    }

    @Provides
    @AppScope
    internal fun provideTaskManager(hyperHive: HyperHive): ITaskManager {
        return TaskManager(hyperHive)
    }

    @Provides
    @AppScope
    internal fun provideTaskBasketRepository(hyperHive: HyperHive, taskManager: ITaskManager): ITaskBasketsRepository {
        return MemoryTaskBasketsRepository(hyperHive, taskManager)
    }

    @Provides
    @AppScope
    internal fun provideFormatter(): IFormatter {
        return Formatter()
    }

    @Provides
    @AppScope
    internal fun provideIRepoInMemoryHolder(): IRepoInMemoryHolder {
        return RepoInMemoryHolder()
    }

    @Provides
    @AppScope
    internal fun provideIBoxesRepository(): IBoxesRepository {
        return MemoryBoxesRepository()
    }

    @Provides
    @AppScope
    internal fun provideICargoUnitRepository(): ICargoUnitRepository {
        return MemoryCargoUnitRepository()
    }
}