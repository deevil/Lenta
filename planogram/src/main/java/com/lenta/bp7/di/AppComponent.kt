package com.lenta.bp7.di


import com.lenta.bp7.ExceptionHandler
import com.lenta.bp7.activity.main.MainActivity
import com.lenta.bp7.activity.main.MainViewModel
import com.lenta.bp7.features.auth.AuthViewModel
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(mainActivity: MainViewModel)
    fun inject(it: ExceptionHandler)
    fun inject(it: AuthViewModel)

}

