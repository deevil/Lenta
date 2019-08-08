package com.lenta.bp9.di

import com.lenta.bp9.ExceptionHandler
import com.lenta.bp9.features.auth.AuthViewModel
import com.lenta.bp9.features.task_list.TaskListViewModel
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
    fun inject(it: TaskListViewModel)
}