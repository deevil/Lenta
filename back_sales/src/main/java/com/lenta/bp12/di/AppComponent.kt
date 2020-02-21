package com.lenta.bp12.di

import com.lenta.bp12.ExceptionHandler
import com.lenta.bp12.main.MainActivity
import com.lenta.bp12.main.MainViewModel
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IGeneralRepository
import com.lenta.bp12.features.auth.AuthViewModel
import com.lenta.bp12.features.enter_employee_number.EnterEmployeeNumberViewModel
import com.lenta.bp12.features.loading.fast.FastLoadingViewModel
import com.lenta.bp12.features.main_menu.MainMenuViewModel
import com.lenta.bp12.features.select_market.SelectMarketViewModel
import com.lenta.bp12.features.task_composition.TaskCompositionViewModel
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent : CoreComponent {

    fun getScreenNavigator(): IScreenNavigator
    fun getGeneralRepository(): IGeneralRepository
    fun getResourceManager(): IResourceManager

    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)

    fun inject(it: ExceptionHandler)
    fun inject(it: AuthViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: FastLoadingViewModel)
    fun inject(it: EnterEmployeeNumberViewModel)
    fun inject(it: MainMenuViewModel)

    fun inject(it: TaskCompositionViewModel)

}