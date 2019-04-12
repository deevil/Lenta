package com.lenta.bp10.di

import com.lenta.bp10.activity.main.MainActivity
import com.lenta.bp10.activity.main.MainViewModel
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.AppScope
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)
}

