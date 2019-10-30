package com.lenta.bp14.di

import com.lenta.bp14.features.price_check.good_info.GoodInfoPcViewModel
import com.lenta.bp14.features.price_check.goods_list.GoodsListPcViewModel
import com.lenta.bp14.features.price_check.price_scanner.PriceScannerViewModel
import com.lenta.bp14.models.check_price.CheckPriceTask
import com.lenta.bp14.models.check_price.CheckPriceTaskDescription
import com.lenta.bp14.models.check_price.ICheckPriceTask
import com.lenta.bp14.models.check_price.repo.*
import com.lenta.bp14.requests.check_price.CheckPriceNetRequest
import com.lenta.bp14.requests.check_price.ICheckPriceNetRequest
import dagger.*
import javax.inject.Scope

@CheckPriceScope
@Component(modules = [CheckPriceModule::class], dependencies = [AppComponent::class])

interface CheckPriceComponent {

    fun getTask(): ICheckPriceTask
    fun inject(it: GoodInfoPcViewModel)
    fun inject(it: GoodsListPcViewModel)
    fun inject(it: PriceScannerViewModel)
}


@Module(includes = [CheckPriceModule.Declarations::class])
class CheckPriceModule(private val taskDescription: CheckPriceTaskDescription) {

    @Module
    internal interface Declarations {

        @Binds
        @CheckPriceScope
        fun bindTask(realisation: CheckPriceTask): ICheckPriceTask

        @Binds
        @CheckPriceScope
        fun bindActualPriceRepo(realisation: ActualPriceRepo): IActualPricesRepo

        @Binds
        @CheckPriceScope
        fun bindCheckPriceNetRequest(realisation: CheckPriceNetRequest): ICheckPriceNetRequest

        @Binds
        @CheckPriceScope
        fun bindCheckPriceResultsRepo(realisation: CheckPriceResultsRepo): ICheckPriceResultsRepo


    }

    @Provides
    @CheckPriceScope
    internal fun provideTaskDescription(): CheckPriceTaskDescription {
        return taskDescription
    }

}

@Scope
@Retention
annotation class CheckPriceScope