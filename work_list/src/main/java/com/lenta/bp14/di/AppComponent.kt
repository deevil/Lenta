package com.lenta.bp14.di

import com.lenta.bp14.ExceptionHandler
import com.lenta.bp14.features.auth.AuthViewModel
import com.lenta.bp14.features.barcode_detection.CoreScanBarCodeViewModel
import com.lenta.bp14.features.job_card.JobCardViewModel
import com.lenta.bp14.features.list_of_differences.ListOfDifferencesViewModel
import com.lenta.bp14.features.loading.fast.FastLoadingViewModel
import com.lenta.bp14.features.main_menu.MainMenuViewModel
import com.lenta.bp14.features.print_settings.PrintSettingsViewModel
import com.lenta.bp14.features.report_result.ReportResultViewModel
import com.lenta.bp14.features.select_market.SelectMarketViewModel
import com.lenta.bp14.features.task_list.TaskListViewModel
import com.lenta.bp14.features.task_list.search_filter.SearchFilterTlViewModel
import com.lenta.bp14.features.search_filter.SearchFilterViewModel
import com.lenta.bp14.main.MainActivity
import com.lenta.bp14.main.MainViewModel
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.check_price.IPriceInfoParser
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.print.IPrintTask
import com.lenta.bp14.platform.IVibrateHelper
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.platform.resource.IResourceManager
import com.lenta.bp14.platform.sound.ISoundPlayer
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent : CoreComponent {

    fun getIScreenNavigator(): IScreenNavigator
    fun getIGeneralTaskManager(): IGeneralTaskManager
    fun getIPriceInfoParser(): IPriceInfoParser
    fun getISoundPlayer(): ISoundPlayer
    fun getIVibrateHelper(): IVibrateHelper
    fun getIGeneralRepo(): IGeneralRepo
    fun getIPrintTask(): IPrintTask
    fun getIResourceManager(): IResourceManager

    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)
    fun inject(it: ExceptionHandler)
    fun inject(it: AuthViewModel)
    fun inject(it: FastLoadingViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: MainMenuViewModel)
    fun inject(it: TaskListViewModel)
    fun inject(it: JobCardViewModel)
    fun inject(it: ListOfDifferencesViewModel)
    fun inject(it: ReportResultViewModel)
    fun inject(it: PrintSettingsViewModel)
    fun inject(it: SearchFilterViewModel)
    fun inject(it: CoreScanBarCodeViewModel)
    fun inject(it: SearchFilterTlViewModel)

}