package com.lenta.movement.di

import com.lenta.movement.features.main_menu.MainMenuViewModel
import com.lenta.movement.ExceptionHandler
import com.lenta.movement.features.goods_info.GoodsInfoViewModel
import com.lenta.movement.features.goods_irrelevant_info.IrrelevantGoodsInfoViewModel
import com.lenta.movement.features.goods_search.GoodsSearchViewModel
import com.lenta.movement.features.goods_without_manufacturer.GoodsWithoutManufacturerViewModel
import com.lenta.movement.features.result.ResultViewModel
import com.lenta.movement.main.MainActivity
import com.lenta.movement.main.MainViewModel
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
    fun inject(it: GoodsSearchViewModel)
    fun inject(it: GoodsWithoutManufacturerViewModel)
    fun inject(it: ResultViewModel)
}

