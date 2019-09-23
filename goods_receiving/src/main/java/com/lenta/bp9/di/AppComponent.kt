package com.lenta.bp9.di

import com.lenta.bp9.ExceptionHandler
import com.lenta.bp9.features.auth.AuthViewModel
import com.lenta.bp9.features.change_datetime.ChangeDateTimeViewModel
import com.lenta.bp9.features.goods_list.GoodsListViewModel
import com.lenta.bp9.features.loading.tasks.LoadingTasksViewModel
import com.lenta.bp9.features.task_list.TaskListViewModel
import com.lenta.bp9.features.loading.fast.FastDataLoadingViewModel
import com.lenta.bp9.features.loading.tasks.LoadingRegisterArrivalViewModel
import com.lenta.bp9.features.loading.tasks.LoadingTaskCardViewModel
import com.lenta.bp9.features.main_menu.MainMenuViewModel
import com.lenta.bp9.features.revise.TaskReviseViewModel
import com.lenta.bp9.features.revise.invoice.InvoiceReviseViewModel
import com.lenta.bp9.features.search_task.SearchTaskViewModel
import com.lenta.bp9.features.select_market.SelectMarketViewModel
import com.lenta.bp9.features.select_personnel_number.SelectPersonnelNumberViewModel
import com.lenta.bp9.features.task_card.TaskCardViewModel
import com.lenta.bp9.main.MainActivity
import com.lenta.bp9.main.MainViewModel
import com.lenta.bp9.model.task.revise.InvoiceRevise
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
    fun inject(it: LoadingTasksViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: FastDataLoadingViewModel)
    fun inject(it: SelectPersonnelNumberViewModel)
    fun inject(it: MainMenuViewModel)
    fun inject(it: GoodsListViewModel)
    fun inject(it: SearchTaskViewModel)
    fun inject(it: TaskCardViewModel)
    fun inject(it: LoadingTaskCardViewModel)
    fun inject(it: ChangeDateTimeViewModel)
    fun inject(it: LoadingRegisterArrivalViewModel)
    fun inject(it: TaskReviseViewModel)
    fun inject(it: InvoiceReviseViewModel)
}