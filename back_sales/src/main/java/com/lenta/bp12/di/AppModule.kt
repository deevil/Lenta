package com.lenta.bp12.di

import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.navigation.ScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.platform.resource.ResourceManager
import com.lenta.bp12.repository.GeneralRepository
import com.lenta.bp12.repository.IGeneralRepository
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
        fun bindGeneralRepository(realisation: GeneralRepository): IGeneralRepository

        @Binds
        @AppScope
        fun bindResourceManager(realisation: ResourceManager): IResourceManager

    }

}