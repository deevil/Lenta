package com.lenta.bp9.di

import com.lenta.bp9.ExceptionHandler
import com.lenta.bp9.features.auth.AuthViewModel
import com.lenta.bp9.features.cargo_unit_card.CargoUnitCardViewModel
import com.lenta.bp9.features.change_datetime.ChangeDateTimeViewModel
import com.lenta.bp9.features.control_delivery_cargo_units.ControlDeliveryCargoUnitsViewModel
import com.lenta.bp9.features.discrepancy_list.DiscrepancyListViewModel
import com.lenta.bp9.features.driver_data.DriverDataViewModel
import com.lenta.bp9.features.editing_invoice.EditingInvoiceViewModel
import com.lenta.bp9.features.formed_docs.FormedDocsViewModel
import com.lenta.bp9.features.goods_details.GoodsDetailsViewModel
import com.lenta.bp9.features.goods_details.marking_goods_details.MarkingGoodsDetailsViewModel
import com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_boxed.ExciseAlcoBoxAccInfoPGEViewModel
import com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_boxed.box_card.ExciseAlcoBoxCardPGEViewModel
import com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_boxed.box_list.ExciseAlcoBoxListPGEViewModel
import com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_stamp.ExciseAlcoStampAccInfoPGEViewModel
import com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_stamp.batch_signs.ExciseAlcoStampPGEBatchSignsViewModel
import com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_boxed.ExciseAlcoBoxAccInfoViewModel
import com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_boxed.box_card.ExciseAlcoBoxCardViewModel
import com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_boxed.box_list.ExciseAlcoBoxListViewModel
import com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_boxed.box_product_failure.ExciseAlcoBoxProductFailureViewModel
import com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_stamp.ExciseAlcoStampAccInfoViewModel
import com.lenta.bp9.features.goods_information.general.task_ppp_pge.GoodsInfoViewModel
import com.lenta.bp9.features.goods_information.general.task_opp.GoodsInfoShipmentPPViewModel
import com.lenta.bp9.features.goods_information.marking.uom_st_without_counting_in_boxes.MarkingInfoViewModel
import com.lenta.bp9.features.goods_information.marking.marking_product_failure.MarkingProductFailureViewModel
import com.lenta.bp9.features.goods_information.marking.uom_st_with_counting_in_boxes.MarkingBoxInfoViewModel
import com.lenta.bp9.features.goods_information.mercury.GoodsMercuryInfoViewModel
import com.lenta.bp9.features.goods_information.non_excise_alco.task_pge.NonExciseAlcoInfoPGEViewModel
import com.lenta.bp9.features.goods_information.non_excise_alco.task_ppp.NonExciseAlcoInfoViewModel
import com.lenta.bp9.features.goods_information.sets.task_pge.NonExciseSetsPGEViewModel
import com.lenta.bp9.features.goods_information.sets.task_pge.set_component_pge.NonExciseSetComponentInfoPGEViewModel
import com.lenta.bp9.features.goods_information.sets.task_ppp.NonExciseSetsReceivingViewModel
import com.lenta.bp9.features.goods_information.sets.task_ppp.set_component_receiving.NonExciseSetComponentInfoReceivingViewModel
import com.lenta.bp9.features.goods_information.z_batches.task_ppp.ZBatchesInfoPPPViewModel
import com.lenta.bp9.features.goods_list.GoodsListViewModel
import com.lenta.bp9.features.input_outgoing_fillings.InputOutgoingFillingsViewModel
import com.lenta.bp9.features.label_printing.LabelPrintingViewModel
import com.lenta.bp9.features.label_printing.print_labels_count_copies.PrintLabelsCountCopiesViewModel
import com.lenta.bp9.features.list_goods_transfer.ListGoodsTransferViewModel
import com.lenta.bp9.features.loading.fast.FastDataLoadingViewModel
import com.lenta.bp9.features.loading.tasks.*
import com.lenta.bp9.features.main_menu.MainMenuViewModel
import com.lenta.bp9.features.mercury_exception_integration.MercuryExceptionIntegrationViewModel
import com.lenta.bp9.features.mercury_list.MercuryListViewModel
import com.lenta.bp9.features.mercury_list_irrelevant.MercuryListIrrelevantViewModel
import com.lenta.bp9.features.reconciliation_mercury.ReconciliationMercuryViewModel
import com.lenta.bp9.features.reject.RejectViewModel
import com.lenta.bp9.features.repres_person_num_entry.RepresPersonNumEntryViewModel
import com.lenta.bp9.features.revise.*
import com.lenta.bp9.features.revise.composite_doc.CompositeDocReviseViewModel
import com.lenta.bp9.features.revise.invoice.InvoiceReviseViewModel
import com.lenta.bp9.features.search_task.SearchTaskViewModel
import com.lenta.bp9.features.select_market.SelectMarketViewModel
import com.lenta.bp9.features.select_personnel_number.SelectPersonnelNumberViewModel
import com.lenta.bp9.features.skip_recount.SkipRecountViewModel
import com.lenta.bp9.features.supply_results.SupplyResultsViewModel
import com.lenta.bp9.features.task_card.TaskCardViewModel
import com.lenta.bp9.features.task_list.TaskListViewModel
import com.lenta.bp9.features.transfer_goods_section.TransferGoodsSectionViewModel
import com.lenta.bp9.features.transport_marriage.TransportMarriageViewModel
import com.lenta.bp9.features.transport_marriage.cargo_unit.TransportMarriageCargoUnitViewModel
import com.lenta.bp9.features.goods_details.transport_marriage_goods_details.TransportMarriageGoodsDetailsViewModel
import com.lenta.bp9.features.goods_information.marking.task_pge.marking_info_box_pge.MarkingInfoBoxPGEViewModel
import com.lenta.bp9.features.transport_marriage.goods_info.TransportMarriageGoodsInfoViewModel
import com.lenta.bp9.features.transportation_number.TransportationNumberViewModel
import com.lenta.bp9.main.MainActivity
import com.lenta.bp9.main.MainViewModel
import com.lenta.shared.di.AppScope
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.FromParentToCoreProvider
import dagger.Component

@Component(modules = [AppModule::class], dependencies = [CoreComponent::class])
@AppScope
interface AppComponent : FromParentToCoreProvider {
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
    fun inject(it: ExciseAlcoStampAccInfoViewModel)
    fun inject(it: LoadingFinishReviseViewModel)
    fun inject(it: LoadingStartReviseViewModel)
    fun inject(it: LoadingUnlockTaskViewModel)
    fun inject(it: TransferGoodsSectionViewModel)
    fun inject(it: TransportConditionsReviseViewModel)
    fun inject(it: LoadingFinishConditionsReviseViewModel)
    fun inject(it: LoadingStartConditionsReviseViewModel)
    fun inject(it: ListGoodsTransferViewModel)
    fun inject(it: RepresPersonNumEntryViewModel)
    fun inject(it: FormedDocsViewModel)
    fun inject(it: LoadingRecountStartViewModel)
    fun inject(it: LoadingSubmittedViewModel)
    fun inject(it: LoadingTransmittedViewModel)
    fun inject(it: EditingInvoiceViewModel)
    fun inject(it: MercuryListViewModel)
    fun inject(it: MercuryListIrrelevantViewModel)
    fun inject(it: ReconciliationMercuryViewModel)
    fun inject(it: MercuryExceptionIntegrationViewModel)
    fun inject(it: GoodsMercuryInfoViewModel)
    fun inject(it: LoadingUnloadingStartRDSViewModel)
    fun inject(it: InputOutgoingFillingsViewModel)
    fun inject(it: ControlDeliveryCargoUnitsViewModel)
    fun inject(it: CargoUnitCardViewModel)
    fun inject(it: SkipRecountViewModel)
    fun inject(it: LoadingRecountStartPGEViewModel)
    fun inject(it: TransportMarriageViewModel)
    fun inject(it: LoadingShipmentPurposeTransportViewModel)
    fun inject(it: TransportationNumberViewModel)
    fun inject(it: DriverDataViewModel)
    fun inject(it: LoadingShipmentArrivalLockViewModel)
    fun inject(it: LoadingShipmentFinishViewModel)
    fun inject(it: LoadingShipmentPostingViewModel)
    fun inject(it: LoadingShipmentStartViewModel)
    fun inject(it: LoadingShipmentFixingDepartureViewModel)
    fun inject(it: LoadingShipmentEndRecountViewModel)
    fun inject(it: CompositeDocReviseViewModel)
    fun inject(it: TransportMarriageCargoUnitViewModel)
    fun inject(it: TransportMarriageGoodsInfoViewModel)
    fun inject(it: TransportMarriageGoodsDetailsViewModel)
    fun inject(it: ExciseAlcoBoxAccInfoViewModel)
    fun inject(it: ExciseAlcoBoxListViewModel)
    fun inject(it: ExciseAlcoBoxCardViewModel)
    fun inject(it: ExciseAlcoBoxProductFailureViewModel)
    fun inject(it: GoodsInfoShipmentPPViewModel)
    fun inject(it: ExciseAlcoBoxAccInfoPGEViewModel)
    fun inject(it: ExciseAlcoBoxListPGEViewModel)
    fun inject(it: ExciseAlcoBoxCardPGEViewModel)
    fun inject(it: ExciseAlcoStampAccInfoPGEViewModel)
    fun inject(it: NonExciseAlcoInfoPGEViewModel)
    fun inject(it: ExciseAlcoStampPGEBatchSignsViewModel)
    fun inject(it: SupplyResultsViewModel)
    fun inject(it: NonExciseSetsPGEViewModel)
    fun inject(it: NonExciseSetsReceivingViewModel)
    fun inject(it: NonExciseSetComponentInfoPGEViewModel)
    fun inject(it: NonExciseSetComponentInfoReceivingViewModel)
    fun inject(it: MarkingInfoViewModel)
    fun inject(it: MarkingGoodsDetailsViewModel)
    fun inject(it: MarkingProductFailureViewModel)
    fun inject(it: MarkingBoxInfoViewModel)
    fun inject(it: ZBatchesInfoPPPViewModel)
    fun inject(it: LabelPrintingViewModel)
    fun inject(it: PrintLabelsCountCopiesViewModel)
    fun inject(it: MarkingInfoBoxPGEViewModel)
}