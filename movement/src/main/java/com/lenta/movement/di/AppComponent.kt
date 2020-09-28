package com.lenta.movement.di

import com.lenta.movement.features.auth.AuthViewModel
import com.lenta.movement.features.loading.fast.FastLoadingViewModel
import com.lenta.movement.features.main.MainMenuViewModel
import com.lenta.movement.features.main.box.GoodsListViewModel
import com.lenta.movement.features.main.box.create.CreateBoxesViewModel
import com.lenta.movement.features.selectmarket.SelectMarketViewModel
import com.lenta.movement.features.selectpersonalnumber.SelectPersonnelNumberViewModel
import com.lenta.movement.features.task.TaskViewModel
import com.lenta.movement.features.task.basket.TaskBasketViewModel
import com.lenta.movement.features.task.basket.info.TaskBasketInfoViewModel
import com.lenta.movement.features.task.eo.TaskEOMergeViewModel
import com.lenta.movement.features.task.eo.eo_insides.TaskEOMergeEOInsidesViewModel
import com.lenta.movement.features.task.eo.formedDocs.TaskEOMergeFormedDocsViewModel
import com.lenta.movement.features.task.eo.ge_insides.TaskEOMergeGEInsidesViewModel
import com.lenta.movement.features.task.goods.TaskGoodsViewModel
import com.lenta.movement.features.task.goods.details.TaskGoodsDetailsViewModel
import com.lenta.movement.features.task.goods.info.TaskGoodsInfoViewModel
import com.lenta.movement.features.task_list.TaskListViewModel
import com.lenta.movement.main.MainActivity
import com.lenta.movement.main.MainViewModel
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import dagger.Component

@Component(modules = [AppModule::class, AppBinds::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)
    fun inject(it: AuthViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: FastLoadingViewModel)
    fun inject(it: SelectPersonnelNumberViewModel)
    fun inject(it: MainMenuViewModel)
    fun inject(it: GoodsListViewModel)
    fun inject(vm: CreateBoxesViewModel)
    fun inject(vm: TaskViewModel)
    fun inject(it: TaskGoodsViewModel)
    fun inject(it: TaskGoodsInfoViewModel)
    fun inject(it: TaskGoodsDetailsViewModel)
    fun inject(it: TaskBasketViewModel)
    fun inject(it: TaskBasketInfoViewModel)
    fun inject(it: TaskEOMergeViewModel)
    fun inject(it: TaskEOMergeFormedDocsViewModel)
    fun inject(it: TaskEOMergeGEInsidesViewModel)
    fun inject(it: TaskEOMergeEOInsidesViewModel)
    fun inject(it: TaskListViewModel)
}