package com.lenta.inventory.di

import com.lenta.bp10.features.auth.AuthViewModel
import com.lenta.inventory.ExceptionHandler
import com.lenta.inventory.main.MainActivity
import com.lenta.inventory.main.MainViewModel
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent {
    fun inject(mainViewModel: MainViewModel)
    fun inject(exceptionHandler: ExceptionHandler)
    fun inject(mainActivity: MainActivity)
    fun inject(it: AuthViewModel)

}

