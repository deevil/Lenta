package com.lenta.bp10.di

import com.lenta.bp10.activity.main.MainActivity
import com.lenta.bp10.activity.main.MainViewModel
import com.lenta.bp10.features.auth.AuthViewModel
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.AppScope
import com.lenta.shared.features.message.MessageViewModel
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)
    fun inject(it: AuthViewModel)
    fun inject(it: MessageViewModel)
}

