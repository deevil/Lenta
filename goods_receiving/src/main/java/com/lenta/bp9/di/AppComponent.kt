package com.lenta.bp9.di

import com.lenta.bp9.ExceptionHandler
import com.lenta.bp9.features.auth.AuthViewModel
import com.lenta.bp9.features.change_datetime.ChangeDateTimeViewModel
import com.lenta.bp9.features.discrepancy_list.DiscrepancyListViewModel
import com.lenta.bp9.features.formed_docs.FormedDocsViewModel
import com.lenta.bp9.features.goods_details.GoodsDetailsViewModel
import com.lenta.bp9.features.goods_information.excise_alco.ExciseAlcoInfoViewModel
import com.lenta.bp9.features.goods_information.general.GoodsInfoViewModel
import com.lenta.bp9.features.goods_information.non_excise_alco.NonExciseAlcoInfoViewModel
import com.lenta.bp9.features.goods_information.perishables.PerishablesInfoViewModel
import com.lenta.bp9.features.goods_list.GoodsListViewModel
import com.lenta.bp9.features.list_goods_transfer.ListGoodsTransferViewModel
import com.lenta.bp9.features.task_list.TaskListViewModel
import com.lenta.bp9.features.loading.fast.FastDataLoadingViewModel
import com.lenta.bp9.features.loading.tasks.*
import com.lenta.bp9.features.main_menu.MainMenuViewModel
import com.lenta.bp9.features.reject.RejectViewModel
import com.lenta.bp9.features.repres_person_num_entry.RepresPersonNumEntryViewModel
import com.lenta.bp9.features.revise.*
import com.lenta.bp9.features.revise.invoice.InvoiceReviseViewModel
import com.lenta.bp9.features.search_task.SearchTaskViewModel
import com.lenta.bp9.features.select_market.SelectMarketViewModel
import com.lenta.bp9.features.select_personnel_number.SelectPersonnelNumberViewModel
import com.lenta.bp9.features.task_card.TaskCardViewModel
import com.lenta.bp9.features.transfer_goods_section.TransferGoodsSectionViewModel
import com.lenta.bp9.main.MainActivity
import com.lenta.bp9.main.MainViewModel
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(mainViewModel: MainViewModel)
    fun inject(it: ExceptionHandler)
    fun inject(it: AuthViewModel)
    fun inject(it: TaskListViewModel)
    fun inject(it: LoadingTasksViewModel)
    fun inject(it: SelectMarketViewModel)
    fun inject(it: FastDataLoadingViewModel)
    fun inject(it: SelectPersonnelNumberViewModel)
    fun inject(it: MainMenuViewModel)
    fun inject(it: GoodsListViewModel)
    fun inject(it: SearchTaskViewModel)
    fun inject(it: TaskCardViewModel)
    fun inject(it: LoadingTaskCardViewModel)
    fun inject(it: ChangeDateTimeViewModel)
    fun inject(it: LoadingRegisterArrivalViewModel)
    fun inject(it: TaskReviseViewModel)
    fun inject(it: GoodsInfoViewModel)
    fun inject(it: GoodsDetailsViewModel)
    fun inject(it: InvoiceReviseViewModel)
    fun inject(it: RejectViewModel)
    fun inject(it: ProductDocumentsReviseViewModel)
    fun inject(it: AlcoholBatchSelectViewModel)
    fun inject(it: AlcoFormReviseViewModel)
    fun inject(it: RussianAlcoFormReviseViewModel)
    fun inject(it: DiscrepancyListViewModel)
    fun inject(it: NonExciseAlcoInfoViewModel)
    fun inject(it: ExciseAlcoInfoViewModel)
    fun inject(it: LoadingFinishReviseViewModel)
    fun inject(it: LoadingStartReviseViewModel)
    fun inject(it: LoadingUnlockTaskViewModel)
    fun inject(it: PerishablesInfoViewModel)
    fun inject(it: TransferGoodsSectionViewModel)
    fun inject(it: TransportConditionsReviseViewModel)
    fun inject(it: LoadingFinishConditionsReviseViewModel)
    fun inject(it: LoadingStartConditionsReviseViewModel)
    fun inject(it: ListGoodsTransferViewModel)
    fun inject(it: RepresPersonNumEntryViewModel)
    fun inject(it: FormedDocsViewModel)
    fun inject(it: LoadingRecountStartViewModel)
}