package com.lenta.bp16.di

import com.lenta.bp16.features.main_menu.MainMenuViewModel
import com.lenta.bp16.ExceptionHandler
import com.lenta.bp16.features.goods_info.GoodsInfoViewModel
import com.lenta.bp16.features.goods_irrelevant_info.IrrelevantGoodsInfoViewModel
import com.lenta.bp16.features.goods_select.GoodsSelectViewModel
import com.lenta.bp16.features.goods_without_manufacturer.GoodsWithoutManufacturerViewModel
import com.lenta.bp16.features.result.ResultViewModel
import com.lenta.bp16.main.MainActivity
import com.lenta.bp16.main.MainViewModel
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.FromParentToCoreProvider
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent : CoreComponent, FromParentToCoreProvider {

    fun inject(mainViewModel: MainViewModel)
    fun inject(mainActivity: MainActivity)

    fun inject(it: ExceptionHandler)
    fun inject(it: MainMenuViewModel)
    fun inject(it: GoodsInfoViewModel)
    fun inject(it: IrrelevantGoodsInfoViewModel)
    fun inject(it: GoodsSelectViewModel)
    fun inject(it: GoodsWithoutManufacturerViewModel)
    fun inject(it: ResultViewModel)
}

