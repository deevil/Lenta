package com.lenta.bp16.di

import com.lenta.bp16.ExceptionHandler
import com.lenta.bp16.features.auth.AuthViewModel
import com.lenta.bp16.features.loading.fast.FastLoadingViewModel
import com.lenta.bp16.features.main_menu.MainMenuViewModel
import com.lenta.bp16.features.select_market.SelectMarketViewModel
import com.lenta.bp16.main.MainActivity
import com.lenta.bp16.main.MainViewModel
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.IGeneralRepo
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent : CoreComponent {

    fun getIScreenNavigator(): IScreenNavigator
    fun getIGeneralRepo(): IGeneralRepo

    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)
    fun inject(it: ExceptionHandler)
    fun inject(it: AuthViewModel)
    fun inject(it: FastLoadingViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: MainMenuViewModel)

}