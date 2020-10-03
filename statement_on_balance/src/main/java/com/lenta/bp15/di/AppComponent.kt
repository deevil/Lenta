package com.lenta.bp15.di

import com.lenta.bp15.ExceptionHandler
import com.lenta.bp15.model.ITaskManager
import com.lenta.bp15.features.auth.AuthViewModel
import com.lenta.bp15.features.discrepancy_list.DiscrepancyListViewModel
import com.lenta.bp15.features.enter_employee_number.EnterEmployeeNumberViewModel
import com.lenta.bp15.features.good_info.GoodInfoViewModel
import com.lenta.bp15.features.good_list.GoodListViewModel
import com.lenta.bp15.features.loading.FastLoadingViewModel
import com.lenta.bp15.features.main_menu.MainMenuViewModel
import com.lenta.bp15.features.search_task.SearchTaskViewModel
import com.lenta.bp15.features.select_market.SelectMarketViewModel
import com.lenta.bp15.features.task_card.TaskCardViewModel
import com.lenta.bp15.features.task_list.TaskListViewModel
import com.lenta.bp15.main.MainActivity
import com.lenta.bp15.main.MainViewModel
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.platform.resource.IResourceManager
import com.lenta.bp15.repository.database.IDatabaseRepository
import com.lenta.bp15.repository.requests.INetRequestsRepository
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent : CoreComponent {

    fun getScreenNavigator(): IScreenNavigator
    fun getResourceManager(): IResourceManager
    fun getDatabaseRepository(): IDatabaseRepository
    fun getNetRequestsRepository(): INetRequestsRepository
    fun getTaskManager(): ITaskManager

    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)

    fun inject(it: ExceptionHandler)
    fun inject(it: AuthViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: FastLoadingViewModel)
    fun inject(it: EnterEmployeeNumberViewModel)
    fun inject(it: MainMenuViewModel)

    fun inject(it: TaskListViewModel)
    fun inject(it: GoodListViewModel)
    fun inject(it: DiscrepancyListViewModel)
    fun inject(it: TaskCardViewModel)
    fun inject(it: GoodInfoViewModel)
    fun inject(it: SearchTaskViewModel)

}