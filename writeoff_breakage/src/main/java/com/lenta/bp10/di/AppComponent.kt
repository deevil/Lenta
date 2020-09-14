package com.lenta.bp10.di

import com.lenta.bp10.ExceptionHandler
import com.lenta.bp10.activity.main.MainActivity
import com.lenta.bp10.activity.main.MainViewModel
import com.lenta.bp10.features.auth.AuthViewModel
import com.lenta.bp10.features.detection_saved_data.DetectionSavedDataViewModel
import com.lenta.bp10.features.good_information.excise_alco.ExciseAlcoInfoViewModel
import com.lenta.bp10.features.good_information.general.GoodInfoViewModel
import com.lenta.bp10.features.good_information.marked.MarkedInfoViewModel
import com.lenta.bp10.features.good_information.sets.SetsViewModel
import com.lenta.bp10.features.good_information.sets.component.ComponentViewModel
import com.lenta.bp10.features.goods_list.GoodsListViewModel
import com.lenta.bp10.features.job_card.JobCardViewModel
import com.lenta.bp10.features.loading.fast.FastLoadingViewModel
import com.lenta.bp10.features.loading.tasks_settings.LoadingTaskSettingsViewModel
import com.lenta.bp10.features.main_menu.MainMenuViewModel
import com.lenta.bp10.features.report_result.ReportResultViewModel
import com.lenta.bp10.features.select_market.SelectMarketViewModel
import com.lenta.bp10.features.select_personnel_number.SelectPersonnelNumberViewModel
import com.lenta.bp10.features.write_off_details.WriteOffDetailsViewModel
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.FromParentToCoreProvider
import com.lenta.shared.features.auxiliary_menu.AuxiliaryMenuViewModel
import com.lenta.shared.features.exit.ExitFromAppViewModel
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.features.printer_change.PrinterChangeViewModel
import com.lenta.shared.features.select_oper_mode.SelectOperModeViewModel
import com.lenta.shared.features.settings.SettingsViewModel
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent : FromParentToCoreProvider {
    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)
    fun inject(it: AuthViewModel)
    fun inject(it: MessageViewModel)
    fun inject(it: ExitFromAppViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: FastLoadingViewModel)
    fun inject(it: SelectPersonnelNumberViewModel)
    fun inject(it: SettingsViewModel)
    fun inject(it: AuxiliaryMenuViewModel)
    fun inject(it: SelectOperModeViewModel)
    fun inject(it: MainMenuViewModel)
    fun inject(it: JobCardViewModel)
    fun inject(it: LoadingTaskSettingsViewModel)
    fun inject(it: PrinterChangeViewModel)
    fun inject(it: GoodsListViewModel)
    fun inject(it: GoodInfoViewModel)
    fun inject(it: ReportResultViewModel)
    fun inject(it: SetsViewModel)
    fun inject(it: WriteOffDetailsViewModel)
    fun inject(it: ComponentViewModel)
    fun inject(it: DetectionSavedDataViewModel)
    fun inject(it: ExceptionHandler)
    fun inject(viewModel: ExciseAlcoInfoViewModel)
    fun inject(it: MarkedInfoViewModel)
}

