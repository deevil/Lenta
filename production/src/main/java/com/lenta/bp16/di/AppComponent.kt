package com.lenta.bp16.di

import com.lenta.bp16.ExceptionHandler
import com.lenta.bp16.data.IPrinter
import com.lenta.bp16.data.IScales
import com.lenta.bp16.features.material_remake_details.add_attribute.MaterialAttributeViewModel
import com.lenta.bp16.features.auth.AuthViewModel
import com.lenta.bp16.features.defect_info.DefectInfoViewModel
import com.lenta.bp16.features.defect_list.DefectListViewModel
import com.lenta.bp16.features.external_supply_list.ExternalSupplyListViewModel
import com.lenta.bp16.features.external_supply_task_list.ExternalSupplyTaskListViewModel
import com.lenta.bp16.features.good_info.GoodInfoViewModel
import com.lenta.bp16.features.good_packaging.GoodPackagingViewModel
import com.lenta.bp16.features.good_weighing.GoodWeighingViewModel
import com.lenta.bp16.features.ingredients_list.IngredientsListViewModel
import com.lenta.bp16.features.ingredient_details.IngredientDetailsViewModel
import com.lenta.bp16.features.ingredient_details.add_attribute.IngredientAttributeViewModel
import com.lenta.bp16.features.loading.fast.FastLoadingViewModel
import com.lenta.bp16.features.main_menu.MainMenuViewModel
import com.lenta.bp16.features.material_remake_details.MaterialRemakeDetailsViewModel
import com.lenta.bp16.features.material_remake_list.MaterialRemakesListViewModel
import com.lenta.bp16.features.order_details.OrderDetailsViewModel
import com.lenta.bp16.features.order_ingredients_list.OrderIngredientsListViewModel
import com.lenta.bp16.features.pack_good_list.PackGoodListViewModel
import com.lenta.bp16.features.pack_list.PackListViewModel
import com.lenta.bp16.features.processing_unit_list.ProcessingUnitListViewModel
import com.lenta.bp16.features.processing_unit_task_list.ProcessingUnitTaskListViewModel
import com.lenta.bp16.features.raw_list.RawListViewModel
import com.lenta.bp16.features.reprint_label.ReprintLabelViewModel
import com.lenta.bp16.features.select_good.GoodSelectViewModel
import com.lenta.bp16.features.select_market.SelectMarketViewModel
import com.lenta.bp16.features.select_personnel_number.SelectPersonnelNumberViewModel
import com.lenta.bp16.features.tech_orders_list.TechOrdersListViewModel
import com.lenta.bp16.features.warehouse_selection.WarehouseSelectionViewModel
import com.lenta.bp16.main.MainActivity
import com.lenta.bp16.main.MainViewModel
import com.lenta.bp16.model.IAttributeManager
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.repository.IDatabaseRepository
import com.lenta.bp16.repository.IIngredientsRepository
import com.lenta.bp16.repository.IMovementRepository
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.FromParentToCoreProvider
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent : CoreComponent, FromParentToCoreProvider {

    fun getScreenNavigator(): IScreenNavigator
    fun getGeneralRepository(): IDatabaseRepository
    fun getMovementRepository(): IMovementRepository
    fun getIngredientsRepository(): IIngredientsRepository
    fun getTaskManager(): ITaskManager
    fun getResourceManager(): IResourceManager
    fun getAttributeManager(): IAttributeManager
    fun getScales(): IScales
    fun getPrinter(): IPrinter

    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)

    fun inject(it: ExceptionHandler)
    fun inject(it: AuthViewModel)
    fun inject(it: FastLoadingViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: MainMenuViewModel)
    fun inject(it: ProcessingUnitTaskListViewModel)
    fun inject(it: ExternalSupplyTaskListViewModel)
    fun inject(it: ProcessingUnitListViewModel)
    fun inject(it: ExternalSupplyListViewModel)
    fun inject(it: RawListViewModel)
    fun inject(it: GoodWeighingViewModel)
    fun inject(it: GoodPackagingViewModel)
    fun inject(it: PackListViewModel)
    fun inject(it: PackGoodListViewModel)
    fun inject(it: ReprintLabelViewModel)
    fun inject(it: DefectInfoViewModel)
    fun inject(it: DefectListViewModel)
    fun inject(it: SelectPersonnelNumberViewModel)
    fun inject(it: GoodInfoViewModel)
    fun inject(it: GoodSelectViewModel)
    fun inject(it: WarehouseSelectionViewModel)
    fun inject(it: IngredientsListViewModel)
    fun inject(it: OrderDetailsViewModel)
    fun inject(it: OrderIngredientsListViewModel)
    fun inject(it: IngredientDetailsViewModel)
    fun inject(it: MaterialRemakesListViewModel)
    fun inject(it: MaterialRemakeDetailsViewModel)
    fun inject(it: TechOrdersListViewModel)
    fun inject(it: MaterialAttributeViewModel)
    fun inject(it: IngredientAttributeViewModel)
}