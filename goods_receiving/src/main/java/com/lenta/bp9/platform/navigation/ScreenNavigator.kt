package com.lenta.bp9.platform.navigation

import android.content.Context
import androidx.core.content.ContextCompat
import com.lenta.bp9.R
import com.lenta.bp9.data.BarcodeParser
import com.lenta.bp9.features.auth.AuthFragment
import com.lenta.bp9.features.cargo_unit_card.CargoUnitCardFragment
import com.lenta.bp9.features.change_datetime.ChangeDateTimeFragment
import com.lenta.bp9.features.change_datetime.ChangeDateTimeMode
import com.lenta.bp9.features.control_delivery_cargo_units.ControlDeliveryCargoUnitsFragment
import com.lenta.bp9.features.discrepancy_list.DiscrepancyListFragment
import com.lenta.bp9.features.driver_data.DriverDataFragment
import com.lenta.bp9.features.editing_invoice.EditingInvoiceFragment
import com.lenta.bp9.features.formed_docs.FormedDocsFragment
import com.lenta.bp9.features.goods_details.GoodsDetailsFragment
import com.lenta.bp9.features.goods_details.marking_goods_details.MarkingGoodsDetailsFragment
import com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_boxed.ExciseAlcoBoxAccInfoPGEFragment
import com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_boxed.box_card.ExciseAlcoBoxCardPGEFragment
import com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_boxed.box_list.ExciseAlcoBoxListPGEFragment
import com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_stamp.ExciseAlcoStampAccInfoPGEFragment
import com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_stamp.batch_signs.ExciseAlcoStampPGEBatchSignsFragment
import com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_boxed.ExciseAlcoBoxAccInfoFragment
import com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_boxed.box_card.ExciseAlcoBoxCardFragment
import com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_boxed.box_list.ExciseAlcoBoxListFragment
import com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_boxed.box_product_failure.ExciseAlcoBoxProductFailureFragment
import com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_stamp.ExciseAlcoStampAccInfoFragment
import com.lenta.bp9.features.goods_information.general.task_ppp_pge.GoodsInfoFragment
import com.lenta.bp9.features.goods_information.general.task_opp.GoodsInfoShipmentPPFragment
import com.lenta.bp9.features.goods_information.marking.uom_st_without_counting_in_boxes.MarkingInfoFragment
import com.lenta.bp9.features.goods_information.marking.marking_product_failure.MarkingProductFailureFragment
import com.lenta.bp9.features.goods_information.marking.uom_st_with_counting_in_boxes.MarkingBoxInfoFragment
import com.lenta.bp9.features.goods_information.mercury.GoodsMercuryInfoFragment
import com.lenta.bp9.features.goods_information.non_excise_alco.task_pge.NonExciseAlcoInfoPGEFragment
import com.lenta.bp9.features.goods_information.non_excise_alco.task_ppp.NonExciseAlcoInfoFragment
import com.lenta.bp9.features.goods_information.sets.task_pge.NonExciseSetsPGEFragment
import com.lenta.bp9.features.goods_information.sets.task_pge.set_component_pge.NonExciseSetComponentInfoPGEFragment
import com.lenta.bp9.features.goods_information.sets.task_ppp.NonExciseSetsReceivingFragment
import com.lenta.bp9.features.goods_information.sets.task_ppp.set_component_receiving.NonExciseSetComponentInfoReceivingFragment
import com.lenta.bp9.features.goods_information.z_batches.task_ppp.ZBatchesInfoPPPFragment
import com.lenta.bp9.features.goods_list.GoodsListFragment
import com.lenta.bp9.features.input_outgoing_fillings.InputOutgoingFillingsFragment
import com.lenta.bp9.features.label_printing.LabelPrintingFragment
import com.lenta.bp9.features.label_printing.LabelPrintingItem
import com.lenta.bp9.features.label_printing.print_labels_count_copies.PrintLabelsCountCopiesFragment
import com.lenta.bp9.features.list_goods_transfer.ListGoodsTransferFragment
import com.lenta.bp9.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp9.features.loading.tasks.*
import com.lenta.bp9.features.main_menu.MainMenuFragment
import com.lenta.bp9.features.mercury_exception_integration.MercuryExceptionIntegrationFragment
import com.lenta.bp9.features.mercury_list.MercuryListFragment
import com.lenta.bp9.features.mercury_list_irrelevant.MercuryListIrrelevantFragment
import com.lenta.bp9.features.reconciliation_mercury.ReconciliationMercuryFragment
import com.lenta.bp9.features.reject.RejectFragment
import com.lenta.bp9.features.repres_person_num_entry.RepresPersonNumEntryFragment
import com.lenta.bp9.features.revise.*
import com.lenta.bp9.features.revise.composite_doc.CompositeDocReviseFragment
import com.lenta.bp9.features.revise.invoice.InvoiceReviseFragment
import com.lenta.bp9.features.search_task.SearchTaskFragment
import com.lenta.bp9.features.select_market.SelectMarketFragment
import com.lenta.bp9.features.select_personnel_number.SelectPersonnelNumberFragment
import com.lenta.bp9.features.skip_recount.SkipRecountFragment
import com.lenta.bp9.features.supply_results.SupplyResultsFragment
import com.lenta.bp9.features.task_card.TaskCardFragment
import com.lenta.bp9.features.task_list.TaskListFragment
import com.lenta.bp9.features.transfer_goods_section.TransferGoodsSectionFragment
import com.lenta.bp9.features.transport_marriage.TransportMarriageFragment
import com.lenta.bp9.features.transport_marriage.cargo_unit.TransportMarriageCargoUnitFragment
import com.lenta.bp9.features.goods_details.transport_marriage_goods_details.TransportMarriageGoodsDetailsFragment
import com.lenta.bp9.features.goods_information.z_batches.task_pge.ZBatchesInfoPGEFragment
import com.lenta.bp9.features.goods_information.marking.task_pge.marking_info_box_pge.MarkingInfoBoxPGEFragment
import com.lenta.bp9.features.transport_marriage.goods_info.TransportMarriageGoodsInfoFragment
import com.lenta.bp9.features.transportation_number.TransportationNumberFragment
import com.lenta.bp9.model.task.*
import com.lenta.bp9.model.task.revise.DeliveryDocumentRevise
import com.lenta.bp9.model.task.revise.DeliveryProductDocumentRevise
import com.lenta.bp9.model.task.revise.ProductDocumentType
import com.lenta.bp9.model.task.revise.ProductVetDocumentRevise
import com.lenta.bp9.platform.navigation.ScreenNavigatorPageNumberConstant.PAGE_NUMBER_94
import com.lenta.bp9.platform.navigation.ScreenNavigatorPageNumberConstant.PAGE_NUMBER_95
import com.lenta.bp9.platform.navigation.ScreenNavigatorPageNumberConstant.PAGE_NUMBER_96
import com.lenta.bp9.platform.navigation.ScreenNavigatorPageNumberConstant.PAGE_NUMBER_97
import com.lenta.bp9.requests.network.TaskListSearchParams
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.models.core.BarcodeData
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.progress.IProgressUseCaseInformator

class ScreenNavigator(
        private val context: Context,
        private val coreNavigator: ICoreNavigator,
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator,
        private val progressUseCaseInformator: IProgressUseCaseInformator
) : IScreenNavigator, ICoreNavigator by coreNavigator {

    override fun openFirstScreen() {
        if (authenticator.isAuthorized()) {
            openMainMenuScreen()
        } else {
            openLoginScreen()
        }
    }

    override fun openSelectMarketScreen() {
        runOrPostpone {
            getFragmentStack()?.replace(SelectMarketFragment())
        }
    }

    override fun openMainMenuScreen() {
        runOrPostpone {
            getFragmentStack()?.replace(MainMenuFragment())
        }
    }

    override fun openLoginScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AuthFragment())
        }
    }

    override fun openTaskListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskListFragment())
        }
    }

    override fun openTaskListLoadingScreen(mode: TaskListLoadingMode, searchParams: TaskListSearchParams?, numberEO: String?) {
        runOrPostpone {
            getFragmentStack()?.push(LoadingTasksFragment.create(searchParams, mode, numberEO))
        }
    }

    override fun openFastDataLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FastDataLoadingFragment())
        }
    }

    override fun openSelectionPersonnelNumberScreen(isScreenMainMenu: Boolean) {
        runOrPostpone {
            getFragmentStack()?.replace(SelectPersonnelNumberFragment.create(isScreenMainMenu))
        }
    }

    override fun openAlertNotPermissions(message: String) {
        openAlertScreen(message = message,
                iconRes = R.drawable.ic_info_pink_80dp,
                textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                pageNumber = "96",
                timeAutoExitInMillis = 3000
        )
    }

    override fun openTaskSearchScreen(loadingMode: TaskListLoadingMode) {
        runOrPostpone {
            getFragmentStack()?.push(SearchTaskFragment.create(loadingMode))
        }
    }

    override fun openGoodsListScreen(taskType: TaskType) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListFragment.create(taskType))
        }
    }

    override fun openTaskCardScreen(mode: TaskCardMode, taskType: TaskType) {
        runOrPostpone {
            getFragmentStack()?.push(TaskCardFragment.create(mode, taskType))
        }
    }

    override fun openTaskCardLoadingScreen(mode: TaskCardMode, taskNumber: String, loadFullData: Boolean) {
        runOrPostpone {
            getFragmentStack()?.push(LoadingTaskCardFragment.create(taskNumber, mode, loadFullData))
        }
    }

    override fun openConfirmationUnlock(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.unlock_confirmation),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openConfirmationView(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.view_confirmation),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openConfirmationUnsavedData(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.unsaved_data_confirmation),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openConfirmationProcessAsDiscrepancy(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.process_as_discrepancy_confirmation),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.process))
        }
    }

    override fun openAlertWithoutConfirmation(message: String, callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = message,
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    pageNumber = "96",
                    leftButtonDecorationInfo = ButtonDecorationInfo.back))
        }
    }

    override fun openChangeDateTimeScreen(mode: ChangeDateTimeMode) {
        runOrPostpone {
            getFragmentStack()?.push(ChangeDateTimeFragment.create(mode))
        }
    }

    override fun openTaskReviseScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskReviseFragment())
        }
    }

    override fun openGoodsInfoScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean, initialCount: Double) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsInfoFragment.create(productInfo, isDiscrepancy, initialCount))
        }
    }

    override fun openAlertWrongProductType() {
        openAlertScreen(message = context.getString(R.string.wrong_product_type),
                iconRes = R.drawable.ic_info_pink_80dp,
                textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                pageNumber = "96",
                timeAutoExitInMillis = 3000
        )
    }

    override fun openGoodsDetailsScreen(productInfo: TaskProductInfo, boxNumberForTaskPGEBoxAlco: String, isScreenPGEBoxAlcoInfo: Boolean) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsDetailsFragment.create(productInfo, boxNumberForTaskPGEBoxAlco, isScreenPGEBoxAlcoInfo))
        }
    }

    override fun openInvoiceReviseScreen() {
        runOrPostpone {
            getFragmentStack()?.push(InvoiceReviseFragment())
        }
    }

    override fun openRejectScreen() {
        runOrPostpone {
            getFragmentStack()?.push(RejectFragment())
        }
    }

    override fun openProductDocumentsReviseScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ProductDocumentsReviseFragment())
        }
    }

    override fun openAlcoholBatchSelectScreen(matnr: String, type: ProductDocumentType) {
        runOrPostpone {
            getFragmentStack()?.push(AlcoholBatchSelectFragment.create(matnr, type))
        }
    }

    override fun openImportAlcoFormReviseScreen(matnr: String, batchNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlcoFormReviseFragment.create(matnr, batchNumber))
        }
    }

    override fun openRussianAlcoFormReviseScreen(matnr: String, batchNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(RussianAlcoFormReviseFragment.create(matnr, batchNumber))
        }
    }

    override fun openDiscrepancyListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(DiscrepancyListFragment())
        }
    }

    override fun openSelectTypeCodeScreen(codeConfirmationForSap: Int, codeConfirmationForBarCode: Int) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.select_type_code_description),
                    iconRes = 0,
                    codeConfirmForRight = codeConfirmationForBarCode,
                    codeConfirmForLeft = codeConfirmationForSap,
                    pageNumber = "90",
                    leftButtonDecorationInfo = ButtonDecorationInfo.sap,
                    rightButtonDecorationInfo = ButtonDecorationInfo.barcode)
            )
        }
    }

    override fun openCheckingNotNeededAlert(message: String, callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = message,
                    iconRes = 0,
                    isVisibleLeftButton = false,
                    timeAutoExitInMillis = 3000,
                    codeConfirmForExit = backFragmentResultHelper.setFuncForResult(callbackFunc))
            )
        }
    }

    override fun openAlertGoodsNotInOrderScreen() {
        openInfoScreen(context.getString(R.string.goods_not_in_order))
    }

    override fun openNonExciseAlcoInfoReceivingScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean) {
        runOrPostpone {
            getFragmentStack()?.push(NonExciseAlcoInfoFragment.create(productInfo, isDiscrepancy))
        }
    }

    override fun openAlertOverLimit() {
        openAlertScreen(message = context.getString(R.string.alert_overlimit),
                iconRes = R.drawable.ic_info_pink_80dp,
                textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                pageNumber = "96",
                timeAutoExitInMillis = 3000
        )
    }

    override fun openExciseAlcoStampAccInfoScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(ExciseAlcoStampAccInfoFragment.create(productInfo))
        }
    }

    override fun openFinishReviseLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingFinishReviseFragment())
        }
    }

    override fun openRegisterArrivalLoadingScreen(isInStockPaperTTN: Boolean, isEdo: Boolean, status: TaskStatus) {
        runOrPostpone {
            getFragmentStack()?.push(LoadingRegisterArrivalFragment.create(isInStockPaperTTN, isEdo, status))
        }
    }

    override fun openStartReviseLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingStartReviseFragment())
        }
    }

    override fun openUnlockTaskLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingUnlockTaskFragment())
        }
    }

    override fun openRoundingIssueDialog(noCallbackFunc: () -> Unit, yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.rounding_issue),
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(noCallbackFunc),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "97",
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openTransferGoodsSectionScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TransferGoodsSectionFragment())
        }
    }

    override fun openTransportConditionsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TransportConditionsReviseFragment())
        }
    }

    override fun openFinishConditionsReviseLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingFinishConditionsReviseFragment())
        }
    }

    override fun openStartConditionsReviseLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingStartConditionsReviseFragment())
        }
    }

    override fun openListGoodsTransferScreen(sectionInfo: TaskSectionInfo) {
        runOrPostpone {
            getFragmentStack()?.push(ListGoodsTransferFragment.create(sectionInfo))
        }
    }

    override fun openRepresPersonNumEntryScreen(sectionInfo: TaskSectionInfo) {
        runOrPostpone {
            getFragmentStack()?.push(RepresPersonNumEntryFragment.create(sectionInfo))
        }
    }

    override fun openFormedDocsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FormedDocsFragment())
        }
    }

    override fun openAlertCountMoreOverdelivery() {
        openAlertScreen(message = context.getString(R.string.alert_count_larger_overdelivery),
                iconRes = R.drawable.ic_info_pink_80dp,
                textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                pageNumber = "96"
        )
    }

    override fun openAlertNotCorrectDate() {
        openAlertScreen(message = context.getString(R.string.alert_not_correct_date),
                iconRes = R.drawable.ic_info_pink_80dp,
                textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                pageNumber = "96"
        )
    }

    override fun openShelfLifeExpiredDialog(yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.the_shelf_life_has_expired),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "97",
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openRecountStartLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingRecountStartFragment())
        }
    }

    override fun openSubmittedLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingSubmittedFragment())
        }
    }

    override fun openTransmittedLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingTransmittedFragment())
        }
    }

    override fun openUnsavedDataDialog(yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.unsaved_data_will_lost),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                    iconRes = R.drawable.ic_delete_red_80dp,
                    pageNumber = "80",
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openEditingInvoiceScreen() {
        runOrPostpone {
            getFragmentStack()?.push(EditingInvoiceFragment())
        }
    }

    override fun openInfoDocsSentPrintScreen() {
        openAlertScreen(message = context.getString(R.string.documents_sent_print),
                iconRes = R.drawable.ic_warning_yellow_80dp,
                pageNumber = "96"
        )
    }

    override fun openAlertGoodsNotInInvoiceScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.goods_not_in_order),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    pageNumber = "96",
                    timeAutoExitInMillis = 3000)
            )
        }
    }

    override fun openOrderQuantityEexceededDialog(noCallbackFunc: () -> Unit, yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.order_quantity_exceeded),
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(noCallbackFunc),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "94",
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openMercuryListScreen(productDoc: DeliveryProductDocumentRevise) {
        runOrPostpone {
            getFragmentStack()?.push(MercuryListFragment.create(productDoc))
        }
    }

    override fun openMercuryListIrrelevantScreen(netRestNumber: Int) {
        runOrPostpone {
            getFragmentStack()?.push(MercuryListIrrelevantFragment.create(netRestNumber))
        }
    }

    override fun openMercuryExceptionIntegrationScreen() {
        runOrPostpone {
            getFragmentStack()?.push(MercuryExceptionIntegrationFragment())
        }
    }

    override fun openReconciliationMercuryScreen(productVetDoc: ProductVetDocumentRevise) {
        runOrPostpone {
            getFragmentStack()?.push(ReconciliationMercuryFragment.create(productVetDoc))
        }
    }

    override fun openGoodsMercuryInfoScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean, barcodeData: BarcodeData?) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsMercuryInfoFragment.newInstance(productInfo, isDiscrepancy, barcodeData))
        }
    }

    override fun openAlertVADProductNotMatchedScreen(productName: String) {
        openInfoScreen(context.getString(R.string.alert_vad_product_not_matched, productName))
    }

    override fun openDiscrepanciesInconsistencyVetDocsDialog(markCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.discrepancies_inconsistency_vet_docs_dialog),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(markCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "79",
                    rightButtonDecorationInfo = ButtonDecorationInfo.mark))
        }
    }

    override fun openDiscrepanciesNoVerifiedVadDialog(excludeCallbackFunc: () -> Unit, markCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.discrepancies_no_vad_dialog),
                    codeConfirmForButton3 = backFragmentResultHelper.setFuncForResult(excludeCallbackFunc),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(markCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "103",
                    buttonDecorationInfo3 = ButtonDecorationInfo.exclude,
                    rightButtonDecorationInfo = ButtonDecorationInfo.mark))
        }
    }

    override fun openAlertQuantGreatInVetDocScreen() {
        openInfoScreen(context.getString(R.string.processing_mercury_quant_great_in_vet_doc))
    }

    override fun openAlertQuantGreatInInvoiceScreen() {
        openInfoScreen(context.getString(R.string.processing_mercury_quant_great_in_invoice))
    }

    override fun openAlertCertificatesLostRelevance(nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.some_certificates_have_lost_relevance),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97",
                    isVisibleLeftButton = false,
                    rightButtonDecorationInfo = ButtonDecorationInfo.next))
        }
    }

    override fun openAlertElectronicVadLostRelevance(browsingCallbackFunc: () -> Unit, countVad: String, countGoods: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.electronic_vad_have_lost_relevance, countVad, countGoods),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(browsingCallbackFunc),
                    pageNumber = "105",
                    rightButtonDecorationInfo = ButtonDecorationInfo.browsingNext))
        }
    }

    override fun openUnloadingStartRDSLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingUnloadingStartRDSFragment())
        }
    }

    override fun openInputOutgoingFillingsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(InputOutgoingFillingsFragment())
        }
    }

    override fun openSealDamageDialog(nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.dialog_seal_damage),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    description = context.getString(R.string.seal_damage),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "95",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openAlertSealDamageScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.alert_seal_damage),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    description = context.getString(R.string.seal_damage),
                    pageNumber = "96",
                    timeAutoExitInMillis = 3000)
            )
        }
    }

    override fun openCargoUnitCardScreen(cargoUnitInfo: TaskCargoUnitInfo, isSurplus: Boolean?) {
        runOrPostpone {
            getFragmentStack()?.push(CargoUnitCardFragment.create(cargoUnitInfo, isSurplus))
        }
    }

    override fun openControlDeliveryCargoUnitsScreen(isUnlockTaskLoadingScreen: Boolean?) {
        runOrPostpone {
            getFragmentStack()?.push(ControlDeliveryCargoUnitsFragment.newInstance(isUnlockTaskLoadingScreen))
        }
    }

    override fun openNewCargoUnitAnotherTransportationDialog(cargoUnitNumber: String, marketNumber: String, nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.dialog_new_cargo_unit_another_transportation, cargoUnitNumber, marketNumber),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    pageNumber = "95",
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openNewCargoUnitCurrentTransportationDialog(cargoUnitNumber: String, marketNumber: String, nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.dialog_new_cargo_unit_current_transportation, cargoUnitNumber, marketNumber),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    pageNumber = "95",
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openAlertNewCargoUnitScreen(cargoUnitNumber: String, marketNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.alert_new_cargo_unit, cargoUnitNumber, marketNumber),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openSkipRecountScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SkipRecountFragment())
        }
    }

    override fun openAlertHaveIsSpecialGoodsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.in_task_special_goods),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    description = context.getString(R.string.skip_recount),
                    pageNumber = "96",
                    timeAutoExitInMillis = 3000)
            )
        }
    }

    override fun openAlertNoIsSpecialGoodsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.in_task_no_special_goods),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    description = context.getString(R.string.skip_recount),
                    pageNumber = "96",
                    timeAutoExitInMillis = 3000)
            )
        }
    }

    override fun openRecountStartPGELoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingRecountStartPGEFragment())
        }
    }

    override fun openTransportMarriageScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TransportMarriageFragment())
        }
    }

    override fun openNoTransportDefectDeclaredDialog(nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.no_transport_defect_declared),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    pageNumber = "95",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openDateNotCorrectlyScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.date_not_correctly),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    pageNumber = "96",
                    timeAutoExitInMillis = 3000)
            )
        }
    }

    override fun openRemainsUnconfirmedBindingDocsPRCDialog(nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.remains_unconfirmed_binding_docs_prc_dialog),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.next))
        }
    }

    override fun openShipmentPurposeTransportLoadingScreen(mode: String, transportationNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(LoadingShipmentPurposeTransportFragment.create(mode, transportationNumber))
        }
    }

    override fun openTransportationNumberScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TransportationNumberFragment())
        }
    }

    override fun openDriverDataScreen() {
        runOrPostpone {
            getFragmentStack()?.push(DriverDataFragment())
        }
    }

    override fun openShipmentArrivalLockLoadingScreen(driverDataInfo: TaskDriverDataInfo) {
        runOrPostpone {
            getFragmentStack()?.push(LoadingShipmentArrivalLockFragment.create(driverDataInfo))
        }
    }

    override fun openShipmentFinishLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingShipmentFinishFragment())
        }
    }

    override fun openShipmentAdjustmentConfirmationDialog(submergedGE: String, nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.dialog_submerged_ge, submergedGE),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    pageNumber = "95",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openShipmentPostingLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingShipmentPostingFragment())
        }
    }

    override fun openShipmentPostingSuccessfulDialog(nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.shipment_posting_successful_dialog),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    iconRes = R.drawable.ic_done_green_80dp,
                    pageNumber = "95",
                    isVisibleLeftButton = false,
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openShipmentStartLoadingScreen(taskNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(LoadingShipmentStartFragment.create(taskNumber))
        }
    }

    override fun openShipmentFixingDepartureLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingShipmentFixingDepartureFragment())
        }
    }

    override fun openShipmentEndRecountLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingShipmentEndRecountFragment())
        }
    }

    override fun openEdoDialog(missing: () -> Unit, inStock: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.edo_dialog),
                    codeConfirmForButton3 = backFragmentResultHelper.setFuncForResult(missing),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(inStock),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "81",
                    buttonDecorationInfo3 = ButtonDecorationInfo.missing,
                    rightButtonDecorationInfo = ButtonDecorationInfo.inStock))
        }
    }

    override fun openAlertMissingVPForProviderScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.alert_missing_vp_for_provider),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    pageNumber = "96",
                    timeAutoExitInMillis = 3000)
            )
        }
    }

    override fun openShipmentConfirmDiscrepanciesDialog(nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.shipment_confirm_discrepancies_dialog),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    pageNumber = "95",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openCompositeDocReviseScreen(document: DeliveryDocumentRevise) {
        runOrPostpone {
            getFragmentStack()?.push(CompositeDocReviseFragment.create(document))
        }
    }

    override fun openTransportMarriageCargoUnitScreen(cargoUnitNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(TransportMarriageCargoUnitFragment.create(cargoUnitNumber))
        }
    }

    override fun openAlertCargoUnitNotFoundScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.cargo_unit_not_found),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openAlertInvalidBarcodeFormatScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.invalid_barcode_format),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    pageNumber = "96",
                    timeAutoExitInMillis = 3000)
            )
        }
    }

    override fun openTransportMarriageGoodsInfoScreen(transportMarriageInfo: TaskTransportMarriageInfo) {
        runOrPostpone {
            getFragmentStack()?.push(TransportMarriageGoodsInfoFragment.create(transportMarriageInfo))
        }
    }

    override fun openTransportMarriageGoodsDetailsScreen(cargoUnitNumber: String, materialNumber: String, materialName: String) {
        runOrPostpone {
            getFragmentStack()?.push(TransportMarriageGoodsDetailsFragment.create(cargoUnitNumber, materialNumber, materialName))
        }
    }

    override fun openAlertAmountEnteredGreaterPUScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.amount_entered_greater_pu),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openAddGoodsSurplusDialog(codeConfirmationAddGoodsSurplus: Int) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.no_product_in_cargo_unit_dialog),
                    iconRes = 0,
                    codeConfirmForRight = codeConfirmationAddGoodsSurplus,
                    pageNumber = "94",
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes)
            )
        }
    }

    override fun openExceededPlannedQuantityBatchInProcessingUnitDialog(nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.exceeded_planned_quantity_batch_in_processing_unit_dialog),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    pageNumber = "95",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openAlertBothSurplusAndUnderloadScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.alert_both_surplus_and_underload),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openAlertCountMoreCargoUnitDialog(yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.alert_count_larger_cargo_unit),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "97",
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openShelfLifeExpiresDialog(noCallbackFunc: () -> Unit, yesCallbackFunc: () -> Unit, expiresThrough: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.the_shelf_life_expires, expiresThrough),
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(noCallbackFunc),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "97",
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openSupplyResultsActDisagreementTransportationDialog(transportationNumber: String, docCallbackFunc: () -> Unit, nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.msg_act_disagreement_transport_marriage, transportationNumber),
                    codeConfirmForButton4 = backFragmentResultHelper.setFuncForResult(docCallbackFunc),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    iconRes = R.drawable.ic_done_green_80dp,
                    pageNumber = "78",
                    description = context.getString(R.string.supply_results),
                    isVisibleLeftButton = false,
                    buttonDecorationInfo4 = ButtonDecorationInfo.docs,
                    rightButtonDecorationInfo = ButtonDecorationInfo.next))
        }
    }

    override fun openExciseAlcoBoxAccInfoReceivingScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(ExciseAlcoBoxAccInfoFragment.create(productInfo))
        }
    }

    override fun openAlertUnknownGoodsTypeScreen() {
        openInfoScreen(context.getString(R.string.unknown_goods_type))
    }

    override fun openCreateInboundDeliveryDialog(yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.create_inbound_delivery_dialog),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "94",
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openAlertUnableSaveNegativeQuantity() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.alert_unable_to_save_negative_quantity),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97",
                    timeAutoExitInMillis = 3000)
            )
        }
    }

    override fun openExciseAlcoBoxListScreen(productInfo: TaskProductInfo, selectQualityCode: String, selectReasonRejectionCode: String?, initialCount: String) {
        runOrPostpone {
            getFragmentStack()?.push(ExciseAlcoBoxListFragment.create(productInfo, selectQualityCode, selectReasonRejectionCode, initialCount))
        }
    }

    override fun openExciseAlcoBoxCardScreen(productInfo: TaskProductInfo, boxInfo: TaskBoxInfo?, massProcessingBoxesNumber: List<String>?, exciseStampInfo: TaskExciseStampInfo?, selectQualityCode: String, selectReasonRejectionCode: String?, initialCount: String, isScan: Boolean) {
        runOrPostpone {
            getFragmentStack()?.push(ExciseAlcoBoxCardFragment.create(productInfo, boxInfo, massProcessingBoxesNumber, exciseStampInfo, selectQualityCode, selectReasonRejectionCode, initialCount, isScan))
        }
    }

    override fun openAlertScannedStampNotFoundScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_stamp_not_listed_in_current_delivery),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openAlertScannedStampBelongsAnotherProductScreen(materialNumber: String, materialName: String) {
        runOrPostpone {
            val materialNumberLastSix = if (materialNumber.length > 6) materialNumber.substring(materialNumber.length - 6) else materialNumber
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_mark_belongs_to_another_product, materialNumberLastSix, materialName),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openAlertRequiredQuantityBoxesAlreadyProcessedScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.required_quantity_boxes_already_processed),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openAlertMustEnterQuantityScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.must_enter_quantity),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openAlertScannedBoxNotFoundScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_box_not_listed_in_current_delivery),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openAlertScannedBoxBelongsAnotherProductScreen(materialNumber: String, materialName: String) {
        runOrPostpone {
            val materialNumberLastSix = if (materialNumber.length > 6) materialNumber.substring(materialNumber.length - 6) else materialNumber
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_box_belongs_to_another_product, materialNumberLastSix, materialName),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openAlertInvalidBarcodeFormatScannedScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.invalid_barcode_format_scanned),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openScannedStampNotFoundDialog(yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_stamp_not_listed_in_current_delivery_box),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = PAGE_NUMBER_95,
                    rightButtonDecorationInfo = ButtonDecorationInfo.next))
        }
    }

    override fun openAlertScannedStampIsAlreadyProcessedScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_stamp_is_already_processed),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openDiscrepancyScannedMarkCurrentBoxDialog(yesCallbackFunc: () -> Unit, currentBoxNumber: String, realBoxNumber: String, paramGrzCrGrundcatName: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.discrepancy_scanned_mark_current_box, currentBoxNumber, realBoxNumber, paramGrzCrGrundcatName),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "97",
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openAlertScannedBoxNotFoundInDeliveryScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.box_not_found_in_delivery),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openAlertNoBoxSelectionRequiredScreen(materialNumber: String, materialName: String, callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.no_box_selection_required, materialNumber, materialName),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    pageNumber = "96",
                    isVisibleLeftButton = false,
                    timeAutoExitInMillis = 3000,
                    codeConfirmForExit = backFragmentResultHelper.setFuncForResult(callbackFunc))
            )
        }
    }

    override fun openAlertMoreBoxesSelectedThanSnteredScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.alert_more_boxes_selected_than_entered),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openExciseAlcoBoxProductFailureScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(ExciseAlcoBoxProductFailureFragment.create(productInfo))
        }
    }

    override fun openCompleteRejectionOfGoodsDialog(applyCallbackFunc: () -> Unit, title: String, countBoxes: String, paramGrzCrGrundcatName: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.dialogue_complete_rejection_of_goods, countBoxes, paramGrzCrGrundcatName),
                    title = title,
                    description = context.getString(R.string.complete_rejection),
                    iconRes = R.drawable.ic_complete_rejection_yellow_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(applyCallbackFunc),
                    rightButtonDecorationInfo = ButtonDecorationInfo.apply,
                    pageNumber = "94")
            )
        }
    }

    override fun openPartialRefusalOnGoodsDialog(applyCallbackFunc: () -> Unit, title: String, countScanBoxes: String, unconfirmedQuantity: String, paramGrzCrGrundcatName: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.dialogue_partial_refusal_on_goods, countScanBoxes, unconfirmedQuantity, paramGrzCrGrundcatName),
                    title = title,
                    description = context.getString(R.string.partial_failure),
                    iconRes = R.drawable.ic_complete_rejection_yellow_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(applyCallbackFunc),
                    rightButtonDecorationInfo = ButtonDecorationInfo.apply,
                    pageNumber = "94")
            )
        }
    }

    override fun openGoodsInfoShipmentPPScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean, initialCount: Double) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsInfoShipmentPPFragment.create(productInfo, isDiscrepancy, initialCount))
        }
    }

    override fun openAlertUnknownTaskTypeScreen() {
        openInfoScreen(context.getString(R.string.unknown_task_type))
    }

    override fun openExciseAlcoBoxAccInfoPGEScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(ExciseAlcoBoxAccInfoPGEFragment.create(productInfo))
        }
    }

    override fun openExciseAlcoBoxListPGEScreen(productInfo: TaskProductInfo, selectQualityCode: String) {
        runOrPostpone {
            getFragmentStack()?.push(ExciseAlcoBoxListPGEFragment.newInstance(productInfo, selectQualityCode))
        }
    }

    override fun openExciseAlcoBoxCardPGEScreen(productInfo: TaskProductInfo, boxInfo: TaskBoxInfo?, massProcessingBoxesNumber: List<String>?, exciseStampInfo: TaskExciseStampInfo?, selectQualityCode: String, isScan: Boolean, isBoxNotIncludedInNetworkLenta: Boolean) {
        runOrPostpone {
            getFragmentStack()?.push(ExciseAlcoBoxCardPGEFragment.create(productInfo, boxInfo, massProcessingBoxesNumber, exciseStampInfo, selectQualityCode, isScan, isBoxNotIncludedInNetworkLenta))
        }
    }

    override fun openAlertOverLimitAlcoPGEScreen(nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.alert_overlimit_alco_pge),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    pageNumber = "95",
                    rightButtonDecorationInfo = ButtonDecorationInfo.next))
        }
    }

    override fun openAlertScannedStampNotFoundTaskPGEScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_stamp_not_in_task_pge),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openScannedBoxListedInCargoUnitDialog(cargoUnitNumber: String, nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_box_is_listed_in_cargo_unit, cargoUnitNumber),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "97",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openScannedBoxNotIncludedInDeliveryDialog(nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_box_not_included_in_delivery),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "97",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openScannedBoxNotIncludedInNetworkLentaDialog(nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_box_is_not_in_network_lenta),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "97",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openScannedStampBoxPGENotFoundDialog(nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_stamp_boxcard_not_in_task_pge),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "97",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openBoxCardUnsavedDataConfirmationDialog(nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.boxcard_unsaved_data_confirmation),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "97",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openAlertAmountNormWillBeReduced() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.amount_of_norm_will_be_reduced),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    pageNumber = "97",
                    timeAutoExitInMillis = 3000))
        }
    }

    override fun openAlertExciseStampPresentInTask() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.excise_stamp_present_in_task),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97"))
        }
    }

    override fun openDiscrepancyScannedMarkCurrentBoxPGEDialog(nextCallbackFunc: () -> Unit, realBoxNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.discrepancy_scanned_mark_current_box_pge, realBoxNumber),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "97",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openExciseAlcoStampAccInfoPGEScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(ExciseAlcoStampAccInfoPGEFragment.newInstance(productInfo))
        }
    }

    override fun openScannedStampListedInCargoUnitDialog(cargoUnitNumber: String, nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_stamp_is_listed_in_cargo_unit, cargoUnitNumber),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "97",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openScannedStampNotIncludedInDeliveryDialog(nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_stamp_not_included_in_delivery),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "97",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openNonExciseAlcoInfoPGEScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean) {
        runOrPostpone {
            getFragmentStack()?.push(NonExciseAlcoInfoPGEFragment.create(productInfo, isDiscrepancy))
        }
    }

    override fun openExceededPlannedQuantityBatchPGEDialog(nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.exceeded_planned_quantity_batch_pge_dialog),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = "97",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openScannedStampNotIncludedInNetworkLentaDialog(title: String) {
        runOrPostpone {
            getFragmentStack()?.push(ExciseAlcoStampPGEBatchSignsFragment.create(title))
        }
    }

    override fun openAlertDeliveryDdataWasSentToGISScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.delivery_data_was_sent_to_GIS),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    pageNumber = "96",
                    timeAutoExitInMillis = 3000)
            )
        }
    }

    override fun openCurrentProviderHasReturnJobsAvailableDialog(numberCurrentProvider: String, nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.current_provider_has_return_jobs_available, numberCurrentProvider),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    iconRes = R.drawable.ic_warning_yellow_80dp,
                    pageNumber = "96",
                    description = context.getString(R.string.supply_results),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next))
        }
    }

    override fun openSupplyResultsScreen(pageNumber: String, numberSupply: String, isAutomaticWriteOff: Boolean) {
        runOrPostpone {
            getFragmentStack()?.push(SupplyResultsFragment.create(pageNumber, numberSupply, isAutomaticWriteOff))
        }
    }

    override fun openNonExciseSetsInfoPGEScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean) {
        runOrPostpone {
            getFragmentStack()?.push(NonExciseSetsPGEFragment.create(productInfo, isDiscrepancy))
        }
    }

    override fun openNonExciseSetComponentInfoPGEScreen(setInfo: TaskSetsInfo, typeDiscrepancies: String, productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(NonExciseSetComponentInfoPGEFragment.create(setInfo, typeDiscrepancies, productInfo))
        }
    }

    override fun openNonExciseSetsInfoReceivingScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean) {
        runOrPostpone {
            getFragmentStack()?.push(NonExciseSetsReceivingFragment.create(productInfo, isDiscrepancy))
        }
    }

    override fun openNonExciseSetComponentInfoReceivingScreen(setInfo: TaskSetsInfo, typeDiscrepancies: String, productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(NonExciseSetComponentInfoReceivingFragment.create(setInfo, typeDiscrepancies, productInfo))
        }
    }

    override fun openAlertGoodsNotFoundTaskScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.goods_not_found),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openAlertAlcocodeNotFoundTaskScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.alcocode_not_found),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openAlertOverLimitPlannedScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.over_limit_planned),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openAlertOverLimitPlannedBatchScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.over_limit_planned_batch),
                    iconRes = R.drawable.ic_info_pink_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openMarkingInfoScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(MarkingInfoFragment.newInstance(productInfo))
        }
    }

    override fun openAlertInvalidCodeScannedForCurrentModeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.invalid_code_scanned_for_current_mode),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = PAGE_NUMBER_97)
            )
        }
    }

    override fun openAlertStampNotFoundReturnSupplierScreen(backCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_stamp_not_listed_in_current_delivery_return_supplier),
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(backCallbackFunc),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = PAGE_NUMBER_97,
                    leftButtonDecorationInfo = ButtonDecorationInfo.back)
            )
        }
    }

    override fun openAlertDisparityGTINScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.disparity_gtin),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = PAGE_NUMBER_97)
            )
        }
    }

    override fun openAlertScanProductBarcodeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scan_product_barcode),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = PAGE_NUMBER_97)
            )
        }
    }

    override fun openAlertGtinDoesNotMatchProductScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.gtin_does_not_match_product),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = PAGE_NUMBER_97)
            )
        }
    }

    override fun openMarkingGoodsDetailsScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(MarkingGoodsDetailsFragment.newInstance(productInfo))
        }
    }

    override fun openAlertRequestCompleteRejectionMarkingGoods(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.request_for_complete_rejection_of_marking_goods),
                    iconRes = R.drawable.ic_info_green_80dp,
                    isVisibleLeftButton = false,
                    timeAutoExitInMillis = 3000,
                    codeConfirmForExit = backFragmentResultHelper.setFuncForResult(callbackFunc))
            )
        }
    }

    override fun openMarkingProductFailureScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(MarkingProductFailureFragment.newInstance(productInfo))
        }
    }

    override fun openCompleteRejectionOfMarkingGoodsDialog(nextCallbackFunc: () -> Unit, title: String, productOrigQuantity: String, paramGrzGrundMarkName: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.dialogue_complete_rejection_of_goods, productOrigQuantity, paramGrzGrundMarkName),
                    title = title,
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next,
                    pageNumber = PAGE_NUMBER_94)
            )
        }
    }

    override fun openPartialRefusalOnMarkingGoodsDialog(nextCallbackFunc: () -> Unit, title: String, confirmedByScanning: String, notConfirmedByScanning: String, paramGrzGrundMarkName: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.dialogue_partial_refusal_on_marking_goods, confirmedByScanning, notConfirmedByScanning, paramGrzGrundMarkName),
                    title = title,
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next,
                    pageNumber = PAGE_NUMBER_94)
            )
        }
    }

    override fun openAlertMustEnterQuantityInfoGreenScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.must_enter_quantity),
                    iconRes = R.drawable.ic_info_green_80dp,
                    pageNumber = PAGE_NUMBER_96,
                    timeAutoExitInMillis = 3000)
            )
        }
    }

    override fun openAlertAmountNormWillBeReducedMarkingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.amount_of_norm_will_be_reduced),
                    iconRes = R.drawable.ic_info_green_80dp,
                    pageNumber = PAGE_NUMBER_97,
                    timeAutoExitInMillis = 3000))
        }
    }

    override fun openAlertScannedStampIsAlreadyProcessedAlternativeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_stamp_is_already_processed_alternative),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = PAGE_NUMBER_97)
            )
        }
    }

    override fun openAlertScanProductGtinScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scan_product_gtin),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = PAGE_NUMBER_97)
            )
        }
    }

    override fun goBackAndShowAlertWrongProductType() {
        goBack()
        openAlertWrongProductType()
    }

    override fun openMarkingBoxInfoScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(MarkingBoxInfoFragment.newInstance(productInfo))
        }
    }

    override fun openMarkingBoxInfoPGEScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(MarkingInfoBoxPGEFragment.newInstance(productInfo))
        }
    }

    override fun openMarkingBoxNotIncludedDeliveryScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.marking_box_not_included_delivery),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = PAGE_NUMBER_97)
            )
        }
    }

    override fun openMarkingPerformRateControlScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.marking_perform_rate_control),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = PAGE_NUMBER_97)
            )
        }
    }

    override fun openMarkingBlockDeclaredDifferentCategoryScreen(typeDiscrepanciesName: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.marking_block_declared_different_category, typeDiscrepanciesName),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = PAGE_NUMBER_97)
            )
        }
    }

    override fun openZBatchesInfoPPPScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean, barcodeData: BarcodeData?) {
        runOrPostpone {
            getFragmentStack()?.push(ZBatchesInfoPPPFragment.newInstance(productInfo, isDiscrepancy, barcodeData))
        }
    }

    override fun openLabelPrintingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LabelPrintingFragment())
        }
    }

    override fun openPrintLabelsCountCopiesScreen(labels: List<LabelPrintingItem>?) {
        runOrPostpone {
            getFragmentStack()?.push(PrintLabelsCountCopiesFragment.newInstance(labels))
        }
    }

    override fun showAlertNoIpPrinter() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = PAGE_NUMBER_96,
                    message = context.getString(R.string.no_ip_printer_alert)
            ))
        }
    }

    override fun openSaveCountedQuantitiesAndGoToLabelPrintingDialog(yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            message = context.getString(R.string.save_counted_quantities_and_go_to_label_printing),
                            iconRes = R.drawable.ic_question_yellow_80dp,
                            codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                            leftButtonDecorationInfo = ButtonDecorationInfo.no,
                            rightButtonDecorationInfo = ButtonDecorationInfo.yes,
                            pageNumber = PAGE_NUMBER_94
                    ),
                    disableAnimations = true
            )
        }
    }

    override fun openAlertNotCorrectTime() {
        openAlertScreen(message = context.getString(R.string.alert_not_correct_time),
                iconRes = R.drawable.ic_info_pink_80dp,
                textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                pageNumber = PAGE_NUMBER_96
        )
    }

    override fun openZBatchesInfoPGEScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean) {
        runOrPostpone {
            getFragmentStack()?.push(ZBatchesInfoPGEFragment.newInstance(productInfo, isDiscrepancy))
        }
    }

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack
}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openSelectMarketScreen()
    fun openMainMenuScreen()
    fun openLoginScreen()
    fun openTaskListScreen()
    fun openTaskListLoadingScreen(mode: TaskListLoadingMode, searchParams: TaskListSearchParams? = null, numberEO: String? = null)
    fun openFastDataLoadingScreen()
    fun openSelectionPersonnelNumberScreen(isScreenMainMenu: Boolean)
    fun openAlertNotPermissions(message: String)
    fun openTaskSearchScreen(loadingMode: TaskListLoadingMode)
    fun openGoodsListScreen(taskType: TaskType)
    fun openTaskCardScreen(mode: TaskCardMode, taskType: TaskType)
    fun openTaskCardLoadingScreen(mode: TaskCardMode, taskNumber: String, loadFullData: Boolean)
    fun openConfirmationUnlock(callbackFunc: () -> Unit)
    fun openConfirmationView(callbackFunc: () -> Unit)
    fun openConfirmationUnsavedData(callbackFunc: () -> Unit)
    fun openConfirmationProcessAsDiscrepancy(callbackFunc: () -> Unit)
    fun openCheckingNotNeededAlert(message: String, callbackFunc: () -> Unit)
    fun openAlertWithoutConfirmation(message: String, callbackFunc: () -> Unit)
    fun openChangeDateTimeScreen(mode: ChangeDateTimeMode)
    fun openTaskReviseScreen()
    fun openGoodsInfoScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean, initialCount: Double = 0.0)
    fun openAlertWrongProductType()
    fun openGoodsDetailsScreen(productInfo: TaskProductInfo, boxNumberForTaskPGEBoxAlco: String = "", isScreenPGEBoxAlcoInfo: Boolean = false)
    fun openInvoiceReviseScreen()
    fun openRejectScreen()
    fun openProductDocumentsReviseScreen()
    fun openAlcoholBatchSelectScreen(matnr: String, type: ProductDocumentType)
    fun openImportAlcoFormReviseScreen(matnr: String, batchNumber: String)
    fun openRussianAlcoFormReviseScreen(matnr: String, batchNumber: String)
    fun openDiscrepancyListScreen()
    fun openSelectTypeCodeScreen(codeConfirmationForSap: Int, codeConfirmationForBarCode: Int)
    fun openAlertGoodsNotInOrderScreen()
    fun openNonExciseAlcoInfoReceivingScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean)
    fun openAlertOverLimit()
    fun openExciseAlcoStampAccInfoScreen(productInfo: TaskProductInfo)
    fun openFinishReviseLoadingScreen()
    fun openRegisterArrivalLoadingScreen(isInStockPaperTTN: Boolean = false, isEdo: Boolean = false, status: TaskStatus = TaskStatus.Other)
    fun openStartReviseLoadingScreen()
    fun openUnlockTaskLoadingScreen()
    fun openTransportConditionsScreen()
    fun openFinishConditionsReviseLoadingScreen()
    fun openStartConditionsReviseLoadingScreen()
    fun openRoundingIssueDialog(noCallbackFunc: () -> Unit, yesCallbackFunc: () -> Unit)
    fun openTransferGoodsSectionScreen()
    fun openListGoodsTransferScreen(sectionInfo: TaskSectionInfo)
    fun openRepresPersonNumEntryScreen(sectionInfo: TaskSectionInfo)
    fun openFormedDocsScreen()
    fun openAlertCountMoreOverdelivery()
    fun openAlertNotCorrectDate()
    fun openShelfLifeExpiredDialog(yesCallbackFunc: () -> Unit)
    fun openRecountStartLoadingScreen()
    fun openSubmittedLoadingScreen()
    fun openTransmittedLoadingScreen()
    fun openUnsavedDataDialog(yesCallbackFunc: () -> Unit)
    fun openEditingInvoiceScreen()
    fun openInfoDocsSentPrintScreen()
    fun openAlertGoodsNotInInvoiceScreen()
    fun openOrderQuantityEexceededDialog(noCallbackFunc: () -> Unit, yesCallbackFunc: () -> Unit)
    fun openMercuryListScreen(productDoc: DeliveryProductDocumentRevise)
    fun openMercuryListIrrelevantScreen(netRestNumber: Int)
    fun openMercuryExceptionIntegrationScreen()
    fun openReconciliationMercuryScreen(productVetDoc: ProductVetDocumentRevise)
    fun openGoodsMercuryInfoScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean, barcodeData: BarcodeData?)
    fun openAlertVADProductNotMatchedScreen(productName: String)
    fun openDiscrepanciesInconsistencyVetDocsDialog(markCallbackFunc: () -> Unit)
    fun openDiscrepanciesNoVerifiedVadDialog(excludeCallbackFunc: () -> Unit, markCallbackFunc: () -> Unit)
    fun openAlertQuantGreatInVetDocScreen()
    fun openAlertQuantGreatInInvoiceScreen()
    fun openAlertCertificatesLostRelevance(nextCallbackFunc: () -> Unit)
    fun openAlertElectronicVadLostRelevance(browsingCallbackFunc: () -> Unit, countVad: String, countGoods: String)
    fun openUnloadingStartRDSLoadingScreen()
    fun openInputOutgoingFillingsScreen()
    fun openSealDamageDialog(nextCallbackFunc: () -> Unit)
    fun openAlertSealDamageScreen()
    fun openCargoUnitCardScreen(cargoUnitInfo: TaskCargoUnitInfo, isSurplus: Boolean? = false)
    fun openControlDeliveryCargoUnitsScreen(isUnlockTaskLoadingScreen: Boolean? = false)
    fun openNewCargoUnitAnotherTransportationDialog(cargoUnitNumber: String, marketNumber: String, nextCallbackFunc: () -> Unit)
    fun openNewCargoUnitCurrentTransportationDialog(cargoUnitNumber: String, marketNumber: String, nextCallbackFunc: () -> Unit)
    fun openAlertNewCargoUnitScreen(cargoUnitNumber: String, marketNumber: String)
    fun openSkipRecountScreen()
    fun openAlertHaveIsSpecialGoodsScreen()
    fun openAlertNoIsSpecialGoodsScreen()
    fun openRecountStartPGELoadingScreen()
    fun openTransportMarriageScreen()
    fun openNoTransportDefectDeclaredDialog(nextCallbackFunc: () -> Unit)
    fun openDateNotCorrectlyScreen()
    fun openRemainsUnconfirmedBindingDocsPRCDialog(nextCallbackFunc: () -> Unit)
    fun openShipmentPurposeTransportLoadingScreen(mode: String, transportationNumber: String)
    fun openTransportationNumberScreen()
    fun openDriverDataScreen()
    fun openShipmentArrivalLockLoadingScreen(driverDataInfo: TaskDriverDataInfo)
    fun openShipmentFinishLoadingScreen()
    fun openShipmentAdjustmentConfirmationDialog(submergedGE: String, nextCallbackFunc: () -> Unit)
    fun openShipmentPostingLoadingScreen()
    fun openShipmentPostingSuccessfulDialog(nextCallbackFunc: () -> Unit)
    fun openShipmentStartLoadingScreen(taskNumber: String)
    fun openShipmentFixingDepartureLoadingScreen()
    fun openShipmentEndRecountLoadingScreen()
    fun openEdoDialog(missing: () -> Unit, inStock: () -> Unit)
    fun openAlertMissingVPForProviderScreen()
    fun openShipmentConfirmDiscrepanciesDialog(nextCallbackFunc: () -> Unit)
    fun openCompositeDocReviseScreen(document: DeliveryDocumentRevise)
    fun openTransportMarriageCargoUnitScreen(cargoUnitNumber: String)
    fun openAlertCargoUnitNotFoundScreen()
    fun openAlertInvalidBarcodeFormatScreen()
    fun openTransportMarriageGoodsInfoScreen(transportMarriageInfo: TaskTransportMarriageInfo)
    fun openTransportMarriageGoodsDetailsScreen(cargoUnitNumber: String, materialNumber: String, materialName: String)
    fun openAlertAmountEnteredGreaterPUScreen()
    fun openAddGoodsSurplusDialog(codeConfirmationAddGoodsSurplus: Int)
    fun openExceededPlannedQuantityBatchInProcessingUnitDialog(nextCallbackFunc: () -> Unit)
    fun openAlertBothSurplusAndUnderloadScreen()
    fun openAlertCountMoreCargoUnitDialog(yesCallbackFunc: () -> Unit)
    fun openShelfLifeExpiresDialog(noCallbackFunc: () -> Unit, yesCallbackFunc: () -> Unit, expiresThrough: String)
    fun openSupplyResultsActDisagreementTransportationDialog(transportationNumber: String, docCallbackFunc: () -> Unit, nextCallbackFunc: () -> Unit)
    fun openExciseAlcoBoxAccInfoReceivingScreen(productInfo: TaskProductInfo)
    fun openAlertUnknownGoodsTypeScreen()
    fun openCreateInboundDeliveryDialog(yesCallbackFunc: () -> Unit)
    fun openAlertUnableSaveNegativeQuantity()
    fun openExciseAlcoBoxListScreen(productInfo: TaskProductInfo, selectQualityCode: String, selectReasonRejectionCode: String?, initialCount: String)
    fun openExciseAlcoBoxCardScreen(productInfo: TaskProductInfo, boxInfo: TaskBoxInfo?, massProcessingBoxesNumber: List<String>?, exciseStampInfo: TaskExciseStampInfo?, selectQualityCode: String, selectReasonRejectionCode: String?, initialCount: String, isScan: Boolean)
    fun openAlertScannedStampNotFoundScreen()
    fun openAlertScannedStampBelongsAnotherProductScreen(materialNumber: String, materialName: String)
    fun openAlertRequiredQuantityBoxesAlreadyProcessedScreen()
    fun openAlertMustEnterQuantityScreen()
    fun openAlertScannedBoxNotFoundScreen()
    fun openAlertScannedBoxBelongsAnotherProductScreen(materialNumber: String, materialName: String)
    fun openAlertInvalidBarcodeFormatScannedScreen()
    fun openScannedStampNotFoundDialog(yesCallbackFunc: () -> Unit)
    fun openAlertScannedStampIsAlreadyProcessedScreen()
    fun openDiscrepancyScannedMarkCurrentBoxDialog(yesCallbackFunc: () -> Unit, currentBoxNumber: String, realBoxNumber: String, paramGrzCrGrundcatName: String)
    fun openAlertScannedBoxNotFoundInDeliveryScreen()
    fun openAlertNoBoxSelectionRequiredScreen(materialNumber: String, materialName: String, callbackFunc: () -> Unit)
    fun openAlertMoreBoxesSelectedThanSnteredScreen()
    fun openExciseAlcoBoxProductFailureScreen(productInfo: TaskProductInfo)
    fun openCompleteRejectionOfGoodsDialog(applyCallbackFunc: () -> Unit, title: String, countBoxes: String, paramGrzCrGrundcatName: String)
    fun openPartialRefusalOnGoodsDialog(applyCallbackFunc: () -> Unit, title: String, countScanBoxes: String, unconfirmedQuantity: String, paramGrzCrGrundcatName: String)
    fun openGoodsInfoShipmentPPScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean, initialCount: Double = 0.0)
    fun openAlertUnknownTaskTypeScreen()
    fun openExciseAlcoBoxAccInfoPGEScreen(productInfo: TaskProductInfo)
    fun openExciseAlcoBoxListPGEScreen(productInfo: TaskProductInfo, selectQualityCode: String)
    fun openExciseAlcoBoxCardPGEScreen(productInfo: TaskProductInfo, boxInfo: TaskBoxInfo?, massProcessingBoxesNumber: List<String>?, exciseStampInfo: TaskExciseStampInfo?, selectQualityCode: String, isScan: Boolean, isBoxNotIncludedInNetworkLenta: Boolean)
    fun openAlertOverLimitAlcoPGEScreen(nextCallbackFunc: () -> Unit)
    fun openAlertScannedStampNotFoundTaskPGEScreen()
    fun openScannedBoxListedInCargoUnitDialog(cargoUnitNumber: String, nextCallbackFunc: () -> Unit)
    fun openScannedBoxNotIncludedInDeliveryDialog(nextCallbackFunc: () -> Unit)
    fun openScannedBoxNotIncludedInNetworkLentaDialog(nextCallbackFunc: () -> Unit)
    fun openScannedStampBoxPGENotFoundDialog(nextCallbackFunc: () -> Unit)
    fun openBoxCardUnsavedDataConfirmationDialog(nextCallbackFunc: () -> Unit)
    fun openAlertAmountNormWillBeReduced()
    fun openAlertExciseStampPresentInTask()
    fun openDiscrepancyScannedMarkCurrentBoxPGEDialog(nextCallbackFunc: () -> Unit, realBoxNumber: String)
    fun openExciseAlcoStampAccInfoPGEScreen(productInfo: TaskProductInfo)
    fun openScannedStampListedInCargoUnitDialog(cargoUnitNumber: String, nextCallbackFunc: () -> Unit)
    fun openScannedStampNotIncludedInDeliveryDialog(nextCallbackFunc: () -> Unit)
    fun openNonExciseAlcoInfoPGEScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean)
    fun openExceededPlannedQuantityBatchPGEDialog(nextCallbackFunc: () -> Unit)
    fun openScannedStampNotIncludedInNetworkLentaDialog(title: String)
    fun openAlertDeliveryDdataWasSentToGISScreen()
    fun openCurrentProviderHasReturnJobsAvailableDialog(numberCurrentProvider: String, nextCallbackFunc: () -> Unit)
    fun openSupplyResultsScreen(pageNumber: String, numberSupply: String, isAutomaticWriteOff: Boolean)
    fun openNonExciseSetsInfoPGEScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean)
    fun openNonExciseSetComponentInfoPGEScreen(setInfo: TaskSetsInfo, typeDiscrepancies: String, productInfo: TaskProductInfo)
    fun openNonExciseSetsInfoReceivingScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean)
    fun openNonExciseSetComponentInfoReceivingScreen(setInfo: TaskSetsInfo, typeDiscrepancies: String, productInfo: TaskProductInfo)
    fun openAlertGoodsNotFoundTaskScreen()
    fun openAlertAlcocodeNotFoundTaskScreen()
    fun openAlertOverLimitPlannedScreen()
    fun openAlertOverLimitPlannedBatchScreen()
    fun openMarkingInfoScreen(productInfo: TaskProductInfo)
    fun openAlertInvalidCodeScannedForCurrentModeScreen()
    fun openAlertStampNotFoundReturnSupplierScreen(backCallbackFunc: () -> Unit)
    fun openAlertDisparityGTINScreen()
    fun openAlertScanProductBarcodeScreen()
    fun openAlertGtinDoesNotMatchProductScreen()
    fun openMarkingGoodsDetailsScreen(productInfo: TaskProductInfo)
    fun openAlertRequestCompleteRejectionMarkingGoods(callbackFunc: () -> Unit)
    fun openMarkingProductFailureScreen(productInfo: TaskProductInfo)
    fun openCompleteRejectionOfMarkingGoodsDialog(nextCallbackFunc: () -> Unit, title: String, productOrigQuantity: String, paramGrzGrundMarkName: String)
    fun openPartialRefusalOnMarkingGoodsDialog(nextCallbackFunc: () -> Unit, title: String, confirmedByScanning: String, notConfirmedByScanning: String, paramGrzGrundMarkName: String)
    fun openAlertMustEnterQuantityInfoGreenScreen()
    fun openAlertAmountNormWillBeReducedMarkingScreen()
    fun openAlertScannedStampIsAlreadyProcessedAlternativeScreen()
    fun openAlertScanProductGtinScreen()
    fun goBackAndShowAlertWrongProductType()
    fun openMarkingBoxInfoScreen(productInfo: TaskProductInfo)
    fun openMarkingBoxInfoPGEScreen(productInfo: TaskProductInfo)
    fun openMarkingBoxNotIncludedDeliveryScreen()
    fun openMarkingPerformRateControlScreen()
    fun openMarkingBlockDeclaredDifferentCategoryScreen(typeDiscrepanciesName: String)
    fun openZBatchesInfoPPPScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean, barcodeData: BarcodeData?)
    fun openLabelPrintingScreen()
    fun openPrintLabelsCountCopiesScreen(labels: List<LabelPrintingItem>? = null)
    fun showAlertNoIpPrinter()
    fun openSaveCountedQuantitiesAndGoToLabelPrintingDialog(yesCallbackFunc: () -> Unit)
    fun openAlertNotCorrectTime()
    fun openZBatchesInfoPGEScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean)
}