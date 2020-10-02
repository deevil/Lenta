package com.lenta.bp14.di

import com.lenta.bp14.features.not_exposed.good_info.GoodInfoNeViewModel
import com.lenta.bp14.features.not_exposed.goods_list.GoodsListNeViewModel
import com.lenta.bp14.features.not_exposed.storage_z_parts.StorageZPartsNotExposedViewModel
import com.lenta.bp14.models.filter.FilterFieldType.*
import com.lenta.bp14.models.filter.FilterableDelegate
import com.lenta.bp14.models.filter.IFilterable
import com.lenta.bp14.models.not_exposed.INotExposedTask
import com.lenta.bp14.models.not_exposed.NotExposedTask
import com.lenta.bp14.models.not_exposed.NotExposedTaskDescription
import com.lenta.bp14.models.not_exposed.NotExposedTaskManager
import com.lenta.bp14.models.not_exposed.repo.INotExposedRepo
import com.lenta.bp14.models.not_exposed.repo.NotExposedRepo
import com.lenta.bp14.platform.resource.IResourceFormatter
import com.lenta.bp14.platform.resource.ResourceFormatter
import com.lenta.bp14.requests.not_exposed_product.IProductInfoForNotExposedNetRequest
import com.lenta.bp14.requests.not_exposed_product.ProductInfoForNotExposedNetRequest
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@NotExposedScope
@Component(modules = [NotExposedModule::class], dependencies = [AppComponent::class])

interface NotExposedComponent {
    fun inject(it: GoodsListNeViewModel)
    fun inject(it: NotExposedTaskManager)
    fun inject(it: GoodInfoNeViewModel)
    fun inject(it: StorageZPartsNotExposedViewModel)
    fun getNotExposedProductsTask(): INotExposedTask
}


@Module(includes = [NotExposedModule.Declarations::class])
class NotExposedModule(private val taskDescription: NotExposedTaskDescription) {

    @Module
    internal interface Declarations {
        @Binds
        @NotExposedScope
        fun bindNotExposedTask(realisation: NotExposedTask): INotExposedTask

        @Binds
        @NotExposedScope
        fun bindNotExposedRepo(realisation: NotExposedRepo): INotExposedRepo

        @Binds
        @NotExposedScope
        fun bindProductInfoForNotExposedNetRequest(realisation: ProductInfoForNotExposedNetRequest): IProductInfoForNotExposedNetRequest

        @Binds
        @NotExposedScope
        fun bindResourceFormatter(realization: ResourceFormatter): IResourceFormatter
    }

    @Provides
    @NotExposedScope
    internal fun provideTaskDescription(): NotExposedTaskDescription {
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