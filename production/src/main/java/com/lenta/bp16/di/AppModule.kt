package com.lenta.bp16.di

import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.navigation.ScreenNavigator
import com.lenta.bp16.repository.GeneralRepo
import com.lenta.bp16.repository.IGeneralRepo
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
        fun bindGeneralRepo(realisation: GeneralRepo): IGeneralRepo

    }

}