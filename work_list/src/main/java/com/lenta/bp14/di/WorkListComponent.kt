package com.lenta.bp14.di

import com.lenta.bp14.features.work_list.good_info.GoodInfoWlViewModel
import com.lenta.bp14.features.work_list.goods_list.GoodsListWlViewModel
import com.lenta.bp14.models.filter.FilterFieldType.*
import com.lenta.bp14.models.filter.FilterableDelegate
import com.lenta.bp14.models.filter.IFilterable
import com.lenta.bp14.models.not_exposed_products.INotExposedProductsTask
import com.lenta.bp14.models.not_exposed_products.NotExposedProductsTask
import com.lenta.bp14.models.not_exposed_products.repo.INotExposedProductsRepo
import com.lenta.bp14.models.not_exposed_products.repo.NotExposedProductsRepo
import com.lenta.bp14.models.work_list.IWorkListTask
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.models.work_list.WorkListTaskDescription
import com.lenta.bp14.models.work_list.WorkListTaskManager
import com.lenta.bp14.models.work_list.repo.IWorkListRepo
import com.lenta.bp14.models.work_list.repo.WorkListRepo
import com.lenta.bp14.requests.not_exposed_product.IProductInfoForNotExposedNetRequest
import com.lenta.bp14.requests.not_exposed_product.ProductInfoForNotExposedNetRequest
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@NotExposedScope
@Component(modules = [WorkListModule::class], dependencies = [AppComponent::class])

interface WorkListComponent {

    fun inject(it: GoodsListWlViewModel)
    fun inject(it: WorkListTaskManager)
    fun inject(it: GoodInfoWlViewModel)
    fun getWorkListTask(): IWorkListTask

}

@Module(includes = [WorkListModule.Declarations::class])
class WorkListModule(private val taskDescription: WorkListTaskDescription) {

    @Module
    internal interface Declarations {

        @Binds
        @WorkListScope
        fun bindWorkListTask(realisation: WorkListTask): IWorkListTask

        @Binds
        @WorkListScope
        fun bindWorkListRepo(realisation: WorkListRepo): IWorkListRepo

    }

    @Provides
    @WorkListScope
    internal fun provideTaskDescription(): WorkListTaskDescription {
        return taskDescription
    }

    @Provides
    @WorkListScope
    internal fun provideFilterableDelegate(): IFilterable {
        return FilterableDelegate(
                supportedFilters = setOf(
                        SECTION,
                        GROUP,
                        PLACE_STORAGE,
                        COMMENT
                )
        )
    }

}

@Scope
@Retention
annotation class WorkListScope