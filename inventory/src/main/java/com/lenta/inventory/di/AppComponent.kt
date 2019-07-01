package com.lenta.inventory.di

import com.lenta.inventory.ExceptionHandler
import com.lenta.inventory.features.auth.AuthViewModel
import com.lenta.inventory.features.discrepancies_found.DiscrepanciesFoundViewModel
import com.lenta.inventory.features.goods_details.GoodsDetailsViewModel
import com.lenta.inventory.features.goods_details_storage.GoodsDetailsStorageViewModel
import com.lenta.inventory.features.goods_information.general.GoodsInfoViewModel
import com.lenta.inventory.features.goods_information.sets.SetsInfoViewModel
import com.lenta.inventory.features.goods_information.sets.components.SetComponentsViewModel
import com.lenta.inventory.features.goods_list.GoodsListViewModel
import com.lenta.inventory.features.loading.fast.FastLoadingViewModel
import com.lenta.inventory.features.main_menu.MainMenuViewModel
import com.lenta.inventory.features.select_market.SelectMarketViewModel
import com.lenta.inventory.features.select_personnel_number.SelectPersonnelNumberViewModel
import com.lenta.inventory.features.sets_details_storage.SetsDetailsStorageViewModel
import com.lenta.inventory.features.storages_list.StoragesListViewModel
import com.lenta.inventory.features.task_list.TaskListViewModel
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
    fun inject(it: SelectMarketViewModel)
    fun inject(it: FastLoadingViewModel)
    fun inject(it: SelectPersonnelNumberViewModel)
    fun inject(it: MainMenuViewModel)
    fun inject(it: GoodsInfoViewModel)
    fun inject(it: GoodsDetailsViewModel)
    fun inject(it: GoodsDetailsStorageViewModel)
    fun inject(it: SetsDetailsStorageViewModel)
    fun inject(it: SetsInfoViewModel)
    fun inject(it: GoodsListViewModel)
    fun inject(it: SetComponentsViewModel)
    fun inject(it: StoragesListViewModel)
    fun inject(it: TaskListViewModel)
    fun inject(it: DiscrepanciesFoundViewModel)
}

