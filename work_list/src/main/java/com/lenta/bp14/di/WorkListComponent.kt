package com.lenta.bp14.di

import com.lenta.bp14.features.work_list.expected_deliveries.ExpectedDeliveriesViewModel
import com.lenta.bp14.features.work_list.good_details.GoodDetailsViewModel
import com.lenta.bp14.features.work_list.good_info.GoodInfoWlViewModel
import com.lenta.bp14.features.work_list.good_sales.GoodSalesViewModel
import com.lenta.bp14.features.work_list.goods_list.GoodsListWlViewModel
import com.lenta.bp14.features.work_list.storage_z_parts.StorageZPartsViewModel
import com.lenta.bp14.models.filter.FilterFieldType.*
import com.lenta.bp14.models.filter.FilterableDelegate
import com.lenta.bp14.models.filter.IFilterable
import com.lenta.bp14.models.work_list.IWorkListTask
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.models.work_list.WorkListTaskDescription
import com.lenta.bp14.models.work_list.WorkListTaskManager
import com.lenta.bp14.models.work_list.repo.IWorkListRepo
import com.lenta.bp14.models.work_list.repo.WorkListRepo
import com.lenta.bp14.platform.resource.IResourceFormatter
import com.lenta.bp14.platform.resource.ResourceFormatter
import com.lenta.bp14.requests.work_list.*
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@WorkListScope
@Component(modules = [WorkListModule::class], dependencies = [AppComponent::class])

interface WorkListComponent {

    fun getTask(): IWorkListTask

    fun inject(it: WorkListTaskManager)
    fun inject(it: GoodsListWlViewModel)
    fun inject(it: GoodInfoWlViewModel)
    fun inject(it: GoodDetailsViewModel)
    fun inject(it: ExpectedDeliveriesViewModel)
    fun inject(it: GoodSalesViewModel)
    fun inject(it: StorageZPartsViewModel)
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

        @Binds
        @WorkListScope
        fun bindExpectedDeliveriesNetRequest(realisation: ExpectedDeliveriesNetRequest): IExpectedDeliveriesNetRequest

        @Binds
        @WorkListScope
        fun bindGoodSalesNetRequest(realisation: GoodSalesNetRequest): IGoodSalesNetRequest

        @Binds
        @WorkListScope
        fun bindAdditionalGoodInfoNetRequest(realisation: AdditionalGoodInfoNetRequest): IAdditionalGoodInfoNetRequest

        @Binds
        @WorkListScope
        fun bindCheckMarkNetRequest(realisation: CheckMarkNetRequest): ICheckMarkNetRequest


        @Binds
        @WorkListScope
        fun bindResourceFormatter(realization: ResourceFormatter): IResourceFormatter

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