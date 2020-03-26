package com.lenta.bp9.platform.navigation

import android.content.Context
import androidx.core.content.ContextCompat
import com.lenta.bp9.R
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
import com.lenta.bp9.features.goods_information.excise_alco.ExciseAlcoInfoFragment
import com.lenta.bp9.features.goods_information.general.GoodsInfoFragment
import com.lenta.bp9.features.goods_information.mercury.GoodsMercuryInfoFragment
import com.lenta.bp9.features.goods_information.non_excise_alco.NonExciseAlcoInfoFragment
import com.lenta.bp9.features.goods_list.GoodsListFragment
import com.lenta.bp9.features.input_outgoing_fillings.InputOutgoingFillingsFragment
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
import com.lenta.bp9.features.task_card.TaskCardFragment
import com.lenta.bp9.features.task_list.TaskListFragment
import com.lenta.bp9.features.transfer_goods_section.TransferGoodsSectionFragment
import com.lenta.bp9.features.transport_marriage.TransportMarriageFragment
import com.lenta.bp9.features.transport_marriage.cargo_unit.TransportMarriageCargoUnitFragment
import com.lenta.bp9.features.transport_marriage.goods_details.TransportMarriageGoodsDetailsFragment
import com.lenta.bp9.features.transport_marriage.goods_info.TransportMarriageGoodsInfoFragment
import com.lenta.bp9.features.transportation_number.TransportationNumberFragment
import com.lenta.bp9.model.task.*
import com.lenta.bp9.model.task.revise.DeliveryDocumentRevise
import com.lenta.bp9.model.task.revise.DeliveryProductDocumentRevise
import com.lenta.bp9.model.task.revise.ProductDocumentType
import com.lenta.bp9.model.task.revise.ProductVetDocumentRevise
import com.lenta.bp9.requests.network.TaskListSearchParams
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.CustomAnimation
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

    override fun openSelectionPersonnelNumberScreen() {
        runOrPostpone {
            getFragmentStack()?.replace(SelectPersonnelNumberFragment())
        }
    }

    override fun openAlertNotPermissions(message: String) {
        openAlertScreen(message = message,
                iconRes = R.drawable.ic_info_pink,
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

    override fun openGoodsListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListFragment())
        }
    }

    override fun openTaskCardScreen(mode: TaskCardMode) {
        runOrPostpone {
            getFragmentStack()?.push(TaskCardFragment.create(mode))
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
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openConfirmationView(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.view_confirmation),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openConfirmationUnsavedData(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.unsaved_data_confirmation),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openConfirmationProcessAsDiscrepancy(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.process_as_discrepancy_confirmation),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
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
                iconRes = R.drawable.ic_info_pink,
                textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                pageNumber = "96",
                timeAutoExitInMillis = 3000
        )
    }

    override fun openGoodsDetailsScreen(productInfo: TaskProductInfo?, batch: TaskBatchInfo?) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsDetailsFragment.create(productInfo, batch))
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

    override fun openNonExciseAlcoInfoScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(NonExciseAlcoInfoFragment.create(productInfo))
        }
    }

    override fun openSupplyResultsSuccessDialog(numberSupply: String, leftCallbackFunc: () -> Unit, rightCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.supply_results_success_dialog, numberSupply),
                    codeConfirmForButton4 = backFragmentResultHelper.setFuncForResult(leftCallbackFunc),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(rightCallbackFunc),
                    iconRes = R.drawable.ic_done_green_80dp,
                    pageNumber = "78",
                    description = context.getString(R.string.supply_results),
                    isVisibleLeftButton = false,
                    rightButtonDecorationInfo = ButtonDecorationInfo.next,
                    buttonDecorationInfo4 = ButtonDecorationInfo.supply))
        }
    }

    override fun openSupplyResultsErrorDialog(numberSupply: String, userName: String) {
        runOrPostpone {
            getFragmentStack()?.let {

                val fragment = AlertFragment.create(
                        message = context.getString(R.string.supply_results_error_dialog, numberSupply, userName),
                        iconRes = R.drawable.ic_info_pink,
                        textColor = ContextCompat.getColor(context, com.lenta.shared.R.color.color_text_dialogWarning),
                        pageNumber = "77",
                        description = context.getString(R.string.supply_results)
                )
                it.push(fragment, CustomAnimation.vertical)

            }
        }
    }

    override fun openSupplyResultsAutomaticChargeSuccessDialog(numberSupply: String, leftCallbackFunc: () -> Unit, rightCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.supply_results_automatic_charge_success, numberSupply),
                    codeConfirmForButton4 = backFragmentResultHelper.setFuncForResult(leftCallbackFunc),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(rightCallbackFunc),
                    iconRes = R.drawable.ic_done_green_80dp,
                    pageNumber = "76",
                    description = context.getString(R.string.supply_results),
                    isVisibleLeftButton = false,
                    rightButtonDecorationInfo = ButtonDecorationInfo.next,
                    buttonDecorationInfo4 = ButtonDecorationInfo.supply))
        }
    }

    override fun openSupplyResultsAutomaticChargeErrorDialog() {
        runOrPostpone {
            getFragmentStack()?.let {

                val fragment = AlertFragment.create(
                        message = context.getString(R.string.supply_results_automatic_charge_error),
                        iconRes = R.drawable.ic_info_pink,
                        textColor = ContextCompat.getColor(context, com.lenta.shared.R.color.color_text_dialogWarning),
                        pageNumber = "75",
                        description = context.getString(R.string.supply_results)
                )
                it.push(fragment, CustomAnimation.vertical)

            }
        }
    }

    override fun openAlertOverlimit() {
        openAlertScreen(message = context.getString(R.string.alert_overlimit),
                iconRes = R.drawable.ic_info_pink,
                textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                pageNumber = "96",
                timeAutoExitInMillis = 3000
        )
    }

    override fun openExciseAlcoInfoScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(ExciseAlcoInfoFragment.create(productInfo))
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
                    iconRes = R.drawable.ic_question_80dp,
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
                iconRes = R.drawable.ic_info_pink,
                textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                pageNumber = "96"
        )
    }

    override fun openAlertNotCorrectDate() {
        openAlertScreen(message = context.getString(R.string.alert_not_correct_date),
                iconRes = R.drawable.ic_info_pink,
                textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                pageNumber = "96"
        )
    }

    override fun openShelfLifeExpiredDialog(yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.the_shelf_life_has_expired),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                    iconRes = R.drawable.ic_question_80dp,
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
                iconRes = R.drawable.is_warning_yellow_80dp,
                pageNumber = "96"
        )
    }

    override fun openAlertGoodsNotInInvoiceScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.goods_not_in_order),
                    iconRes = R.drawable.is_warning_yellow_80dp,
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
                    iconRes = R.drawable.ic_question_80dp,
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

    override fun openGoodsMercuryInfoScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsMercuryInfoFragment.create(productInfo, isDiscrepancy))
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
                    iconRes = R.drawable.ic_question_80dp,
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
                    iconRes = R.drawable.ic_question_80dp,
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
                    iconRes = R.drawable.ic_info_pink,
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
                    pageNumber = "95",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openAlertSealDamageScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.alert_seal_damage),
                    iconRes = R.drawable.is_warning_yellow_80dp,
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

    override fun openControlDeliveryCargoUnitsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ControlDeliveryCargoUnitsFragment())
        }
    }

    override fun openNewCargoUnitAnotherTransportationDialog(cargoUnitNumber: String, nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.dialog_new_cargo_unit_another_transportation, cargoUnitNumber),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    pageNumber = "95",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openNewCargoUnitCurrentTransportationDialog(cargoUnitNumber: String, nextCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.dialog_new_cargo_unit_current_transportation, cargoUnitNumber),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallbackFunc),
                    pageNumber = "95",
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate))
        }
    }

    override fun openAlertNewCargoUnitScreen(cargoUnitNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.alert_new_cargo_unit, cargoUnitNumber),
                    iconRes = R.drawable.ic_info_pink,
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
                    iconRes = R.drawable.is_warning_yellow_80dp,
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
                    iconRes = R.drawable.is_warning_yellow_80dp,
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
                    iconRes = R.drawable.is_warning_yellow_80dp,
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
                    message = submergedGE,
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
                    iconRes = R.drawable.ic_question_80dp,
                    pageNumber = "81",
                    buttonDecorationInfo3 = ButtonDecorationInfo.missing,
                    rightButtonDecorationInfo = ButtonDecorationInfo.inStock))
        }
    }

    override fun openAlertMissingVPForProviderScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.alert_missing_vp_for_provider),
                    iconRes = R.drawable.is_warning_yellow_80dp,
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
                    iconRes = R.drawable.ic_info_pink,
                    textColor = ContextCompat.getColor(context, R.color.color_text_dialogWarning),
                    pageNumber = "97")
            )
        }
    }

    override fun openAlertInvalidBarcodeFormatScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.invalid_barcode_format),
                    iconRes = R.drawable.is_warning_yellow_80dp,
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
                    iconRes = R.drawable.ic_info_pink,
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
                    iconRes = R.drawable.ic_info_pink,
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
                    iconRes = R.drawable.ic_question_80dp,
                    pageNumber = "97",
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openShelfLifeExpiresDialog(noCallbackFunc: () -> Unit, yesCallbackFunc: () -> Unit, expiresThrough: String, shelfLife: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.the_shelf_life_expires, expiresThrough, shelfLife),
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(noCallbackFunc),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallbackFunc),
                    iconRes = R.drawable.ic_question_80dp,
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
    fun openSelectionPersonnelNumberScreen()
    fun openAlertNotPermissions(message: String)
    fun openTaskSearchScreen(loadingMode: TaskListLoadingMode)
    fun openGoodsListScreen()
    fun openTaskCardScreen(mode: TaskCardMode)
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
    fun openGoodsDetailsScreen(productInfo: TaskProductInfo? = null, batch: TaskBatchInfo? = null)
    fun openInvoiceReviseScreen()
    fun openRejectScreen()
    fun openProductDocumentsReviseScreen()
    fun openAlcoholBatchSelectScreen(matnr: String, type: ProductDocumentType)
    fun openImportAlcoFormReviseScreen(matnr: String, batchNumber: String)
    fun openRussianAlcoFormReviseScreen(matnr: String, batchNumber: String)
    fun openDiscrepancyListScreen()
    fun openSelectTypeCodeScreen(codeConfirmationForSap: Int, codeConfirmationForBarCode: Int)
    fun openAlertGoodsNotInOrderScreen()
    fun openNonExciseAlcoInfoScreen(productInfo: TaskProductInfo)
    fun openSupplyResultsErrorDialog(numberSupply: String, userName: String)
    fun openSupplyResultsSuccessDialog(numberSupply: String, leftCallbackFunc: () -> Unit, rightCallbackFunc: () -> Unit)
    fun openSupplyResultsAutomaticChargeErrorDialog()
    fun openSupplyResultsAutomaticChargeSuccessDialog(numberSupply: String, leftCallbackFunc: () -> Unit, rightCallbackFunc: () -> Unit)
    fun openAlertOverlimit()
    fun openExciseAlcoInfoScreen(productInfo: TaskProductInfo)
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
    fun openGoodsMercuryInfoScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean)
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
    fun openControlDeliveryCargoUnitsScreen()
    fun openNewCargoUnitAnotherTransportationDialog(cargoUnitNumber: String, nextCallbackFunc: () -> Unit)
    fun openNewCargoUnitCurrentTransportationDialog(cargoUnitNumber: String, nextCallbackFunc: () -> Unit)
    fun openAlertNewCargoUnitScreen(cargoUnitNumber: String)
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
    fun openShelfLifeExpiresDialog(noCallbackFunc: () -> Unit, yesCallbackFunc: () -> Unit, expiresThrough: String, shelfLife: String)
    fun openSupplyResultsActDisagreementTransportationDialog(transportationNumber: String, docCallbackFunc: () -> Unit, nextCallbackFunc: () -> Unit)
}