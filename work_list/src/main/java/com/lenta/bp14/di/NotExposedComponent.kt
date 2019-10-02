package com.lenta.bp14.di

import com.lenta.bp14.features.not_exposed.good_info.GoodInfoNeViewModel
import com.lenta.bp14.features.not_exposed.goods_list.GoodsListNeViewModel
import com.lenta.bp14.models.check_price.repo.ActualPriceRepo
import com.lenta.bp14.models.check_price.repo.IActualPricesRepo
import com.lenta.bp14.models.filter.FilterFieldType.*
import com.lenta.bp14.models.filter.FilterableDelegate
import com.lenta.bp14.models.filter.IFilterable
import com.lenta.bp14.models.not_exposed_products.INotExposedProductsTask
import com.lenta.bp14.models.not_exposed_products.NotExposedProductsTask
import com.lenta.bp14.models.not_exposed_products.NotExposedProductsTaskDescription
import com.lenta.bp14.models.not_exposed_products.NotExposedProductsTaskManager
import com.lenta.bp14.models.not_exposed_products.repo.INotExposedProductsRepo
import com.lenta.bp14.models.not_exposed_products.repo.NotExposedProductsRepo
import com.lenta.bp14.requests.not_exposed_product.IProductInfoForNotExposedNetRequest
import com.lenta.bp14.requests.not_exposed_product.ProductInfoForNotExposedNetRequest
import dagger.*
import javax.inject.Scope

@NotExposedScope
@Component(modules = [NotExposedModule::class], dependencies = [AppComponent::class])

interface NotExposedComponent {
    fun inject(it: GoodsListNeViewModel)
    fun inject(it: NotExposedProductsTaskManager)
    fun inject(it: GoodInfoNeViewModel)
    fun getNotExposedProductsTask(): INotExposedProductsTask
}


@Module(includes = [NotExposedModule.Declarations::class])
class NotExposedModule(private val taskDescription: NotExposedProductsTaskDescription) {

    @Module
    internal interface Declarations {
        @Binds
        @NotExposedScope
        fun bindNotExposedProductsTask(realisation: NotExposedProductsTask): INotExposedProductsTask

        @Binds
        @NotExposedScope
        fun bindNotExposedProductsRepo(realisation: NotExposedProductsRepo): INotExposedProductsRepo

        @Binds
        @NotExposedScope
        fun bindProductInfoForNotExposedNetRequest(realisation: ProductInfoForNotExposedNetRequest): IProductInfoForNotExposedNetRequest
    }

    @Provides
    @NotExposedScope
    internal fun provideTaskDescription(): NotExposedProductsTaskDescription {
        return taskDescription
    }

    @Provides
    @NotExposedScope
    internal fun provideFilterableDelegate(): IFilterable {
        return FilterableDelegate(
                supportedFilters = setOf(
                        NUMBER,
                        SECTION,
                        GROUP
                )
        )
    }


}

@Scope
@Retention
annotation class NotExposedScope