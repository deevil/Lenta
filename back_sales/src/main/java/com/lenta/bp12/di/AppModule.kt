package com.lenta.bp12.di

import com.lenta.bp12.managers.CreateTaskManager
import com.lenta.bp12.managers.GeneralTaskManager
import com.lenta.bp12.managers.MarkManager
import com.lenta.bp12.managers.OpenTaskManager
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.managers.interfaces.IGeneralTaskManager
import com.lenta.bp12.managers.interfaces.IMarkManager
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.navigation.ScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.platform.resource.ResourceManager
import com.lenta.bp12.repository.DatabaseRepository
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.repository.IRepoInMemoryHolder
import com.lenta.bp12.repository.RepoInMemoryHolder
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
        fun bindGeneralRepository(realisation: DatabaseRepository): IDatabaseRepository

        @Binds
        @AppScope
        fun bindResourceManager(realisation: ResourceManager): IResourceManager

        @Binds
        @AppScope
        fun bindCreateTaskManager(realisation: CreateTaskManager): ICreateTaskManager

        @Binds
        @AppScope
        fun bindOpenTaskManager(realisation: OpenTaskManager): IOpenTaskManager

        @Binds
        @AppScope
        fun bindGeneralTaskManager(realisation: GeneralTaskManager): IGeneralTaskManager

        @Binds
        @AppScope
        fun bindMarkManager(realisation: MarkManager): IMarkManager

    }

}