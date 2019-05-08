package com.lenta.bp10.di

import com.lenta.bp10.activity.main.MainActivity
import com.lenta.bp10.activity.main.MainViewModel
import com.lenta.bp10.features.auth.AuthViewModel
import com.lenta.bp10.features.auxiliary_menu.AuxiliaryMenuViewModel
import com.lenta.bp10.features.good_information.general.GoodInfoViewModel
import com.lenta.bp10.features.goods_list.GoodsListViewModel
import com.lenta.bp10.features.job_card.JobCardViewModel
import com.lenta.bp10.features.loading.fast.FastLoadingViewModel
import com.lenta.bp10.features.loading.tasks_settings.LoadingTaskSettingsViewModel
import com.lenta.bp10.features.main_menu.MainMenuViewModel
import com.lenta.bp10.features.select_market.SelectMarketViewModel
import com.lenta.bp10.features.select_oper_mode.SelectOperModeViewModel
import com.lenta.bp10.features.select_personnel_number.SelectPersonnelNumberViewModel
import com.lenta.bp10.features.settings.SettingsViewModel
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.features.message.MessageViewModel
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)
    fun inject(it: AuthViewModel)
    fun inject(it: MessageViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: FastLoadingViewModel)
    fun inject(it: SelectPersonnelNumberViewModel)
    fun inject(it: SettingsViewModel)
    fun inject(it: AuxiliaryMenuViewModel)
    fun inject(it: SelectOperModeViewModel)
    fun inject(it: MainMenuViewModel)
    fun inject(it: JobCardViewModel)
    fun inject(it: LoadingTaskSettingsViewModel)
    fun inject(it: GoodsListViewModel)
    fun inject(it: GoodInfoViewModel)
}

