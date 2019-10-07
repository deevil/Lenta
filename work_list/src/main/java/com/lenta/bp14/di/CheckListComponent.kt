package com.lenta.bp14.di

import com.lenta.bp14.features.check_list.goods_list.GoodsListClViewModel
import com.lenta.bp14.models.check_list.CheckListTask
import com.lenta.bp14.models.check_list.CheckListTaskDescription
import com.lenta.bp14.models.check_list.CheckListTaskManager
import com.lenta.bp14.models.check_list.ICheckListTask
import com.lenta.bp14.models.check_list.repo.CheckListRepo
import com.lenta.bp14.models.check_list.repo.ICheckListRepo
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@CheckListScope
@Component(modules = [CheckListModule::class], dependencies = [AppComponent::class])

interface CheckListComponent {

    fun inject(it: GoodsListClViewModel)
    fun inject(it: CheckListTaskManager)
    fun getCheckListTask(): ICheckListTask

}

@Module(includes = [CheckListModule.Declarations::class])
class CheckListModule(private val taskDescription: CheckListTaskDescription) {

    @Module
    internal interface Declarations {

        @Binds
        @CheckPriceScope
        fun bindCheckListTask(realisation: CheckListTask): ICheckListTask

        @Binds
        @CheckPriceScope
        fun bindCheckListRepo(realisation: CheckListRepo): ICheckListRepo

    }

    @Provides
    @CheckPriceScope
    internal fun provideTaskDescription(): CheckListTaskDescription {
        return taskDescription
    }

}

@Scope
@Retention
annotation class CheckListScope