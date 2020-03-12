package com.lenta.bp12.di

import com.lenta.bp12.ExceptionHandler
import com.lenta.bp12.features.add_supplier.AddSupplierViewModel
import com.lenta.bp12.main.MainActivity
import com.lenta.bp12.main.MainViewModel
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.features.auth.AuthViewModel
import com.lenta.bp12.features.basket_good_list.BasketGoodListViewModel
import com.lenta.bp12.features.basket_properties.BasketPropertiesViewModel
import com.lenta.bp12.features.discrepancy_list.DiscrepancyListViewModel
import com.lenta.bp12.features.enter_employee_number.EnterEmployeeNumberViewModel
import com.lenta.bp12.features.good_details.GoodDetailsViewModel
import com.lenta.bp12.features.good_info.GoodInfoViewModel
import com.lenta.bp12.features.good_list.GoodListViewModel
import com.lenta.bp12.features.loading.fast.FastLoadingViewModel
import com.lenta.bp12.features.main_menu.MainMenuViewModel
import com.lenta.bp12.features.save_data.SaveDataViewModel
import com.lenta.bp12.features.select_market.SelectMarketViewModel
import com.lenta.bp12.features.task_card_create.TaskCardCreateViewModel
import com.lenta.bp12.features.task_card_open.TaskCardOpenViewModel
import com.lenta.bp12.features.task_composition.TaskCompositionViewModel
import com.lenta.bp12.features.task_list.TaskListViewModel
import com.lenta.bp12.features.task_search.TaskSearchViewModel
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent : CoreComponent {

    fun getScreenNavigator(): IScreenNavigator
    fun getGeneralRepository(): IDatabaseRepository
    fun getResourceManager(): IResourceManager
    fun getCreateTaskManager(): ICreateTaskManager
    fun getOpenTaskManager(): IOpenTaskManager

    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)

    fun inject(it: ExceptionHandler)
    fun inject(it: AuthViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: FastLoadingViewModel)
    fun inject(it: EnterEmployeeNumberViewModel)
    fun inject(it: MainMenuViewModel)

    fun inject(it: TaskCompositionViewModel)
    fun inject(it: BasketGoodListViewModel)
    fun inject(it: GoodDetailsViewModel)
    fun inject(it: SaveDataViewModel)
    fun inject(it: TaskListViewModel)
    fun inject(it: GoodListViewModel)
    fun inject(it: DiscrepancyListViewModel)
    fun inject(it: BasketPropertiesViewModel)
    fun inject(it: TaskSearchViewModel)
    fun inject(it: TaskCardCreateViewModel)
    fun inject(it: TaskCardOpenViewModel)
    fun inject(it: GoodInfoViewModel)
    fun inject(it: AddSupplierViewModel)

}