package com.lenta.bp18.di

import com.lenta.bp18.ExceptionHandler
import com.lenta.bp18.features.auth.AuthViewModel
import com.lenta.bp18.features.goods_info.GoodsInfoViewModel
import com.lenta.bp18.features.select_goods.SelectGoodsViewModel
import com.lenta.bp18.features.select_market.SelectMarketViewModel
import com.lenta.bp18.main.MainActivity
import com.lenta.bp18.main.MainViewModel
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.FromParentToCoreProvider
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent : CoreComponent, FromParentToCoreProvider {

    fun getScreenNavigator(): IScreenNavigator

    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)

    fun inject(it: ExceptionHandler)
    fun inject(it: AuthViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: GoodsInfoViewModel)
    fun inject(it: SelectGoodsViewModel)

}