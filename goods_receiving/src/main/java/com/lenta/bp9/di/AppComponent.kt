package com.lenta.bp9.di

import com.lenta.bp9.ExceptionHandler
import com.lenta.bp9.features.auth.AuthViewModel
import com.lenta.bp9.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp9.features.loading.fast.FastDataLoadingViewModel
import com.lenta.bp9.features.select_market.SelectMarketViewModel
import com.lenta.bp9.features.select_personnel_number.SelectPersonnelNumberViewModel
import com.lenta.bp9.main.MainActivity
import com.lenta.bp9.main.MainViewModel
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)
    fun inject(it: ExceptionHandler)
    fun inject(it: AuthViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: FastDataLoadingViewModel)
    fun inject(it: SelectPersonnelNumberViewModel)

}