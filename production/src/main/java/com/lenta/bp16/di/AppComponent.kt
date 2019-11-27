package com.lenta.bp16.di

import com.lenta.bp16.ExceptionHandler
import com.lenta.bp16.features.auth.AuthViewModel
import com.lenta.bp16.features.good_packaging.GoodPackagingViewModel
import com.lenta.bp16.features.good_weighing.GoodWeighingViewModel
import com.lenta.bp16.features.raw_good_list.RawGoodListViewModel
import com.lenta.bp16.features.loading.fast.FastLoadingViewModel
import com.lenta.bp16.features.main_menu.MainMenuViewModel
import com.lenta.bp16.features.pack_good_list.PackGoodListViewModel
import com.lenta.bp16.features.pack_list.PackListViewModel
import com.lenta.bp16.features.raw_list.RawListViewModel
import com.lenta.bp16.features.select_market.SelectMarketViewModel
import com.lenta.bp16.features.task_list.TaskListViewModel
import com.lenta.bp16.main.MainActivity
import com.lenta.bp16.main.MainViewModel
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.IGeneralRepository
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent : CoreComponent {

    fun getScreenNavigator(): IScreenNavigator
    fun getGeneralRepository(): IGeneralRepository
    fun getTaskManager(): ITaskManager

    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)

    fun inject(it: ExceptionHandler)
    fun inject(it: AuthViewModel)
    fun inject(it: FastLoadingViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: MainMenuViewModel)
    fun inject(it: TaskListViewModel)
    fun inject(it: RawGoodListViewModel)
    fun inject(it: RawListViewModel)
    fun inject(it: GoodWeighingViewModel)
    fun inject(it: GoodPackagingViewModel)
    fun inject(it: PackListViewModel)
    fun inject(it: PackGoodListViewModel)

}