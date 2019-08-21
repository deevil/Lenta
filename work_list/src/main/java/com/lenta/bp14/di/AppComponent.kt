package com.lenta.bp14.di

import com.lenta.bp14.ExceptionHandler
import com.lenta.bp14.features.auth.AuthViewModel
import com.lenta.bp14.features.check_list.goods_list.GoodsListClViewModel
import com.lenta.bp14.features.work_list.good_info.GoodInfoWlViewModel
import com.lenta.bp14.features.job_card.JobCardViewModel
import com.lenta.bp14.features.list_of_differences.ListOfDifferencesViewModel
import com.lenta.bp14.features.loading.fast.FastLoadingViewModel
import com.lenta.bp14.features.main_menu.MainMenuViewModel
import com.lenta.bp14.features.not_displayed_goods.NotDisplayedGoodsInfoViewModel
import com.lenta.bp14.features.not_displayed_goods.NotDisplayedGoodsViewModel
import com.lenta.bp14.features.price_check.good_info.GoodInfoPcViewModel
import com.lenta.bp14.features.price_check.goods_list.GoodsListPcViewModel
import com.lenta.bp14.features.print_settings.PrintSettingsViewModel
import com.lenta.bp14.features.report_result.ReportResultViewModel
import com.lenta.bp14.features.select_market.SelectMarketViewModel
import com.lenta.bp14.features.task_list.TaskListViewModel
import com.lenta.bp14.features.work_list.details_of_goods.DetailsOfGoodsViewModel
import com.lenta.bp14.features.work_list.expected_deliveries.ExpectedDeliveriesViewModel
import com.lenta.bp14.features.work_list.goods_list.GoodsListWlViewModel
import com.lenta.bp14.features.work_list.sales_of_goods.SalesOfGoodsViewModel
import com.lenta.bp14.features.work_list.search_filter.SearchFilterWlViewModel
import com.lenta.bp14.main.MainActivity
import com.lenta.bp14.main.MainViewModel
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
    fun inject(it: FastLoadingViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: MainMenuViewModel)
    fun inject(it: TaskListViewModel)
    fun inject(it: JobCardViewModel)
    fun inject(it: GoodsListClViewModel)
    fun inject(it: ListOfDifferencesViewModel)
    fun inject(it: ReportResultViewModel)
    fun inject(it: PrintSettingsViewModel)
    fun inject(it: GoodsListWlViewModel)
    fun inject(it: GoodInfoWlViewModel)
    fun inject(it: GoodInfoPcViewModel)
    fun inject(it: DetailsOfGoodsViewModel)
    fun inject(it: GoodsListPcViewModel)
    fun inject(it: ExpectedDeliveriesViewModel)
    fun inject(it: SearchFilterWlViewModel)
    fun inject(it: SalesOfGoodsViewModel)
    fun inject(it: NotDisplayedGoodsViewModel)
    fun inject(it: NotDisplayedGoodsInfoViewModel)

}