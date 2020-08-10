package com.lenta.bp18.di

import com.lenta.bp18.ExceptionHandler
import com.lenta.bp18.data.model.CheckData
import com.lenta.bp18.features.auth.AuthViewModel
import com.lenta.bp18.features.good_info.GoodInfoViewModel
import com.lenta.bp18.features.loading.fast.FastLoadingViewModel
import com.lenta.bp18.features.select_good.SelectGoodViewModel
import com.lenta.bp18.features.select_market.SelectMarketViewModel
import com.lenta.bp18.main.MainActivity
import com.lenta.bp18.main.MainViewModel
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IDatabaseRepo
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.FromParentToCoreProvider
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent : FromParentToCoreProvider {

    fun getScreenNavigator(): IScreenNavigator
    fun getGeneralRepository(): IDatabaseRepo
    fun getCheckStoreData(): CheckData

    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)

    fun inject(it: AuthViewModel)
    fun inject(it: FastLoadingViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: SelectGoodViewModel)
    fun inject(it: GoodInfoViewModel)
    fun inject(it: ExceptionHandler)

}