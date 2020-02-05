package com.lenta.bp16.di

import com.lenta.bp16.data.IPrinter
import com.lenta.bp16.data.IScales
import com.lenta.bp16.data.Printer
import com.lenta.bp16.data.Scales
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.model.TaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.navigation.ScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.platform.resource.ResourceManager
import com.lenta.bp16.repo.DatabaseRepository
import com.lenta.bp16.repo.IDatabaseRepository
import com.lenta.bp16.repository.GeneralRepository
import com.lenta.bp16.repository.IGeneralRepository
import com.lenta.bp16.repository.IRepoInMemoryHolder
import com.lenta.bp16.repository.RepoInMemoryHolder
import com.lenta.shared.di.AppScope
import dagger.Binds
import dagger.Module

@Module(includes = [AppModule.Declarations::class])
class AppModule {

    @Module
    internal interface Declarations {

        @Binds
        @AppScope
        fun bindRepoInMemoryHolder(realisation: RepoInMemoryHolder): IRepoInMemoryHolder

        @Binds
        @AppScope
        fun bindScreenNavigator(realisation: ScreenNavigator): IScreenNavigator

        @Binds
        @AppScope
        fun bindGeneralRepository(realisation: GeneralRepository): IGeneralRepository

        @Binds
        @AppScope
        fun bindTaskManager(realisation: TaskManager): ITaskManager

        @Binds
        @AppScope
        fun bindResourceManager(realisation: ResourceManager): IResourceManager

        @Binds
        @AppScope
        fun bindScales(realisation: Scales): IScales

        @Binds
        @AppScope
        fun bindPrinter(realisation: Printer): IPrinter

        @Binds
        @AppScope
        fun bindDatabaseRepository(realisation: DatabaseRepository): IDatabaseRepository

    }

}