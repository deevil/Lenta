package com.lenta.bp9.platform.navigation

import android.content.Context
import android.provider.Settings.Global.getString
import androidx.core.content.ContextCompat
import com.lenta.bp9.features.auth.AuthFragment
import com.lenta.bp9.features.goods_list.GoodsListFragment
import com.lenta.bp9.features.task_list.TaskListFragment
import com.lenta.bp9.requests.network.TaskListSearchParams
import com.lenta.bp9.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp9.features.main_menu.MainMenuFragment
import com.lenta.bp9.features.search_task.SearchTaskFragment
import com.lenta.bp9.features.select_market.SelectMarketFragment
import com.lenta.bp9.features.select_personnel_number.SelectPersonnelNumberFragment
import com.lenta.bp9.features.task_card.TaskCardFragment
import com.lenta.bp9.R
import com.lenta.bp9.features.cargo_unit_card.CargoUnitCardFragment
import com.lenta.bp9.features.change_datetime.ChangeDateTimeFragment
import com.lenta.bp9.features.change_datetime.ChangeDateTimeMode
import com.lenta.bp9.features.control_delivery_cargo_units.ControlDeliveryCargoUnitsFragment
import com.lenta.bp9.features.discrepancy_list.DiscrepancyListFragment
import com.lenta.bp9.features.editing_invoice.EditingInvoiceFragment
import com.lenta.bp9.features.formed_docs.FormedDocsFragment
import com.lenta.bp9.features.goods_details.GoodsDetailsFragment
import com.lenta.bp9.features.goods_information.excise_alco.ExciseAlcoInfoFragment
import com.lenta.bp9.features.goods_information.general.GoodsInfoFragment
import com.lenta.bp9.features.goods_information.mercury.GoodsMercuryInfoFragment
import com.lenta.bp9.features.goods_information.non_excise_alco.NonExciseAlcoInfoFragment
import com.lenta.bp9.features.goods_information.perishables.PerishablesInfoFragment
import com.lenta.bp9.features.input_outgoing_fillings.InputOutgoingFillingsFragment
import com.lenta.bp9.features.list_goods_transfer.ListGoodsTransferFragment
import com.lenta.bp9.features.loading.tasks.*
import com.lenta.bp9.features.mercury_exception_integration.MercuryExceptionIntegrationFragment
import com.lenta.bp9.features.mercury_list.MercuryListFragment
import com.lenta.bp9.features.mercury_list_irrelevant.MercuryListIrrelevantFragment
import com.lenta.bp9.features.reconciliation_mercury.ReconciliationMercuryFragment
import com.lenta.bp9.features.reject.RejectFragment
import com.lenta.bp9.features.repres_person_num_entry.RepresPersonNumEntryFragment
import com.lenta.bp9.features.revise.*
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.features.revise.invoice.InvoiceReviseFragment
import com.lenta.bp9.features.transfer_goods_section.TransferGoodsSectionFragment
import com.lenta.bp9.model.task.revise.ProductDocumentType
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskCargoUnitInfo
import com.lenta.bp9.model.task.TaskSectionInfo
import com.lenta.bp9.model.task.revise.DeliveryProductDocumentRevise
import com.lenta.bp9.model.task.revise.ProductVetDocumentRevise
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
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

    override fun openTaskListLoadingScreen(mode: TaskListLoadingMode, searchParams: TaskListSearchParams?) {
        runOrPostpone {
            getFragmentStack()?.push(LoadingTasksFragment.create(searchParams, mode))
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

    override fun openTaskSearchScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SearchTaskFragment())
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

    override fun openLoadingRegisterArrivalScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingRegisterArrivalFragment())
        }
    }

    override fun openTaskReviseScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskReviseFragment())
        }
    }

    override fun openGoodsInfoScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsInfoFragment.create(productInfo, isDiscrepancy))
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

    override fun openRegisterArrivalLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingRegisterArrivalFragment())
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

    override fun openPerishablesInfoScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(PerishablesInfoFragment.create(productInfo))
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

    override fun openAlertCountLargerOverdelivery() {
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

    override fun openExpiredDialog(noCallbackFunc: () -> Unit, yesCallbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.the_shelf_life_has_expired),
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(noCallbackFunc),
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

    override fun openAlertNotFoundTaskScreen(failure: Failure) {
        runOrPostpone {
            getFragmentStack()?.pop()
            openAlertScreen(failure)
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

    override fun openInfoDocsSentPScreenrint() {
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

    override fun openAlertQuantGreatInInvoiceScreen() {
        openInfoScreen(context.getString(R.string.processing_mercury_quant_great_in_invoice))
    }

    override fun openAlertQuantGreatInOrderScreen() {
        openInfoScreen(context.getString(R.string.processing_mercury_quant_great_in_order))
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

    override fun openCargoUnitCardScreen(cargoUnitInfo: TaskCargoUnitInfo) {
        runOrPostpone {
            getFragmentStack()?.push(CargoUnitCardFragment.create(cargoUnitInfo))
        }
    }

    override fun openControlDeliveryCargoUnitsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ControlDeliveryCargoUnitsFragment())
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
    fun openTaskListLoadingScreen(mode: TaskListLoadingMode, searchParams: TaskListSearchParams? = null)
    fun openFastDataLoadingScreen()
    fun openSelectionPersonnelNumberScreen()
    fun openAlertNotPermissions(message: String)
    fun openTaskSearchScreen()
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
    fun openLoadingRegisterArrivalScreen()
    fun openTaskReviseScreen()
    fun openGoodsInfoScreen(productInfo: TaskProductInfo, isDiscrepancy: Boolean)
    fun openAlertWrongProductType()
    fun openGoodsDetailsScreen(productInfo: TaskProductInfo? = null, batch: TaskBatchInfo? =null)
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
    fun openRegisterArrivalLoadingScreen()
    fun openStartReviseLoadingScreen()
    fun openUnlockTaskLoadingScreen()
    fun openTransportConditionsScreen()
    fun openFinishConditionsReviseLoadingScreen()
    fun openStartConditionsReviseLoadingScreen()
    fun openPerishablesInfoScreen(productInfo: TaskProductInfo)
    fun openRoundingIssueDialog(noCallbackFunc: () -> Unit, yesCallbackFunc: () -> Unit)
    fun openTransferGoodsSectionScreen()
    fun openListGoodsTransferScreen(sectionInfo: TaskSectionInfo)
    fun openRepresPersonNumEntryScreen(sectionInfo: TaskSectionInfo)
    fun openFormedDocsScreen()
    fun openAlertCountLargerOverdelivery()
    fun openAlertNotCorrectDate()
    fun openExpiredDialog(noCallbackFunc: () -> Unit, yesCallbackFunc: () -> Unit)
    fun openRecountStartLoadingScreen()
    fun openSubmittedLoadingScreen()
    fun openTransmittedLoadingScreen()
    fun openAlertNotFoundTaskScreen(failure: Failure)
    fun openUnsavedDataDialog(yesCallbackFunc: () -> Unit)
    fun openEditingInvoiceScreen()
    fun openInfoDocsSentPScreenrint()
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
    fun openAlertQuantGreatInInvoiceScreen()
    fun openAlertQuantGreatInOrderScreen()
    fun openAlertCertificatesLostRelevance(nextCallbackFunc: () -> Unit)
    fun openAlertElectronicVadLostRelevance(browsingCallbackFunc: () -> Unit, countVad: String, countGoods: String)
    fun openUnloadingStartRDSLoadingScreen()
    fun openInputOutgoingFillingsScreen()
    fun openSealDamageDialog(nextCallbackFunc: () -> Unit)
    fun openControlDeliveryCargoUnitsScreen()
    fun openCargoUnitCardScreen(cargoUnitInfo: TaskCargoUnitInfo)
}