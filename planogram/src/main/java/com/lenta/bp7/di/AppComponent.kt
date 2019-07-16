package com.lenta.bp7.di

import com.lenta.bp7.ExceptionHandler
import com.lenta.bp7.activity.main.MainActivity
import com.lenta.bp7.activity.main.MainViewModel
import com.lenta.bp7.data.model.CheckStoreData
import com.lenta.bp7.features.auth.AuthViewModel
import com.lenta.bp7.features.check_type.CheckTypeViewModel
import com.lenta.bp7.features.code.CodeViewModel
import com.lenta.bp7.features.good_info.GoodInfoViewModel
import com.lenta.bp7.features.good_list.GoodListViewModel
import com.lenta.bp7.features.loading.fast.FastLoadingViewModel
import com.lenta.bp7.features.option.OptionViewModel
import com.lenta.bp7.features.segment_list.SegmentListViewModel
import com.lenta.bp7.features.select_market.SelectMarketViewModel
import com.lenta.bp7.features.shelf_list.ShelfListViewModel
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent {

    fun getCheckStoreData(): CheckStoreData

    fun inject(mainActivity: MainActivity)
    fun inject(mainActivity: MainViewModel)
    fun inject(it: ExceptionHandler)
    fun inject(it: AuthViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: FastLoadingViewModel)
    fun inject(it: CheckTypeViewModel)
    fun inject(it: CodeViewModel)
    fun inject(it: OptionViewModel)
    fun inject(it: SegmentListViewModel)
    fun inject(it: ShelfListViewModel)
    fun inject(it: GoodListViewModel)
    fun inject(it: GoodInfoViewModel)

}

