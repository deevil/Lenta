package com.lenta.bp15.di

import com.lenta.bp15.model.ITaskManager
import com.lenta.bp15.model.TaskManager
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.platform.navigation.ScreenNavigator
import com.lenta.bp15.platform.resource.IResourceManager
import com.lenta.bp15.platform.resource.ResourceManager
import com.lenta.bp15.repository.IRepoInMemoryHolder
import com.lenta.bp15.repository.RepoInMemoryHolder
import com.lenta.bp15.repository.database.DatabaseRepository
import com.lenta.bp15.repository.database.IDatabaseRepository
import com.lenta.bp15.repository.persist.IPersistRepository
import com.lenta.bp15.repository.persist.PersistRepository
import com.lenta.bp15.repository.requests.INetRequestsRepository
import com.lenta.bp15.repository.requests.NetRequestsRepository
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
        fun bindDatabaseRepository(realisation: DatabaseRepository): IDatabaseRepository

        @Binds
        @AppScope
        fun bindNetRequestsRepository(realisation: NetRequestsRepository): INetRequestsRepository

        @Binds
        @AppScope
        fun bindPersistTaskData(realisation: PersistRepository): IPersistRepository

        @Binds
        @AppScope
        fun bindScreenNavigator(realisation: ScreenNavigator): IScreenNavigator

        @Binds
        @AppScope
        fun bindResourceManager(realisation: ResourceManager): IResourceManager

        @Binds
        @AppScope
        fun bindTaskManager(realisation: TaskManager): ITaskManager

    }

}