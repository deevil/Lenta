package com.lenta.bp10.platform.navigation

import android.content.Context
import androidx.core.content.ContextCompat
import com.lenta.bp10.R
import com.lenta.bp10.features.auth.AuthFragment
import com.lenta.bp10.features.detection_saved_data.DetectionSavedDataFragment
import com.lenta.bp10.features.good_information.excise_alco.ExciseAlcoInfoFragment
import com.lenta.bp10.features.good_information.general.GoodInfoFragment
import com.lenta.bp10.features.good_information.marked.MarkedInfoFragment
import com.lenta.bp10.features.good_information.sets.ComponentItem
import com.lenta.bp10.features.good_information.sets.SetsFragment
import com.lenta.bp10.features.good_information.sets.component.ComponentFragment
import com.lenta.bp10.features.goods_list.GoodsListFragment
import com.lenta.bp10.features.job_card.JobCardFragment
import com.lenta.bp10.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp10.features.loading.tasks_settings.LoadingTaskSettingsFragment
import com.lenta.bp10.features.main_menu.MainMenuFragment
import com.lenta.bp10.features.report_result.ReportResultFragment
import com.lenta.bp10.features.select_market.SelectMarketFragment
import com.lenta.bp10.features.select_personnel_number.SelectPersonnelNumberFragment
import com.lenta.bp10.features.write_off_details.WriteOffDetailsFragment
import com.lenta.bp10.progress.IWriteOffProgressUseCaseInformator
import com.lenta.bp10.requests.network.SendWriteOffDataResult
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.features.printer_change.PrinterChangeFragment
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo

class ScreenNavigator(
        private val context: Context,
        private val coreNavigator: ICoreNavigator,
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator,
        private val progressUseCaseInformator: IWriteOffProgressUseCaseInformator
) : IScreenNavigator, ICoreNavigator by coreNavigator {

    override fun openSelectMarketScreen() {
        runOrPostpone {
            getFragmentStack()?.replace(SelectMarketFragment())
        }
    }

    override fun openFirstScreen() {
        if (authenticator.isAuthorized()) {
            openSelectMarketScreen()
        } else {
            openLoginScreen()
        }
    }

    override fun openLoginScreen() {
        runOrPostpone {
            getFragmentStack()?.let {
                it.popAll()
                it.replace(AuthFragment())
            }
        }
    }

    override fun openFastDataLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FastDataLoadingFragment())
        }

    }

    override fun openSelectionPersonnelNumberScreen(codeConfirmation: Int?, isScreenMainMenu: Boolean) {
        runOrPostpone {
            getFragmentStack()?.push(SelectPersonnelNumberFragment.create(codeConfirmation = codeConfirmation, isScreenMainMenu = isScreenMainMenu))
        }
    }


    override fun <Params> showProgress(useCase: UseCase<Any, Params>) {
        runOrPostpone {
            showProgress(progressUseCaseInformator.getTitle(useCase))
        }
    }

    override fun openMainMenuScreen() {
        runOrPostpone {
            getFragmentStack()?.replace(MainMenuFragment())
        }
    }

    override fun openJobCardScreen() {
        runOrPostpone {
            getFragmentStack()?.push(JobCardFragment())
        }
    }

    override fun openLoadingTaskSettingsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingTaskSettingsFragment())
        }
    }

    override fun openGoodsListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListFragment())
        }
    }

    override fun openGoodInfoScreen(productInfo: ProductInfo, quantity: Double) {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoFragment.newInstance(productInfo, quantity))
        }
    }

    override fun openMarkedInfoScreen(productInfo: ProductInfo, quantity: Double) {
        runOrPostpone {
            getFragmentStack()?.push(MarkedInfoFragment.create(productInfo, quantity))
        }
    }

    override fun openExciseAlcoScreen(productInfo: ProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(ExciseAlcoInfoFragment.newInstance(productInfo))
        }
    }

    override fun openSetsInfoScreen(productInfo: ProductInfo, quantity: Double) {
        runOrPostpone {
            getFragmentStack()?.push(SetsFragment.newInstance(productInfo, quantity))
        }
    }

    override fun openPrinterChangeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(PrinterChangeFragment())
        }
    }

    override fun openComponentSetScreen(productInfo: ProductInfo, componentItem: ComponentItem, targetTotalCount: Double) {
        runOrPostpone {

            if (productInfo.type != ProductType.ExciseAlcohol) {
                openInfoScreen(context.getString(R.string.not_support_type_product_for_set))
                return@runOrPostpone
            }

            getFragmentStack()?.push(ComponentFragment.newInstance(
                    productInfo = productInfo,
                    componentItem = componentItem,
                    targetTotalCount = targetTotalCount
            )
            )
        }
    }

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

    override fun openRemoveTaskConfirmationScreen(taskDescription: String, codeConfirmation: Int) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.remove_task_confirmation, taskDescription),
                    iconRes = R.drawable.ic_delete_red_80dp, codeConfirmForRight = codeConfirmation, pageNumber = "88"))
        }
    }

    override fun openMatrixAlertScreen(matrixType: MatrixType, yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(if (matrixType == MatrixType.Deleted) R.string.allert_deleted_matrix_message else R.string.allert_unknown_matrix_message),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes)
            )
        }
    }

    override fun openRemoveLinesConfirmationScreen(taskDescription: String, count: Int, codeConfirmation: Int) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.remove_lines_confirmation, count),
                    iconRes = R.drawable.ic_delete_red_80dp, codeConfirmForRight = codeConfirmation, pageNumber = "89"))
        }
    }

    override fun openRemoveItemsFromSelectedCategory(count: Int, applyCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.remove_lines_confirmation, count),
                    iconRes = R.drawable.ic_delete_red_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(applyCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.apply)
            )
        }
    }

    override fun openSendingReportsScreen(sendWriteOffDataResult: SendWriteOffDataResult) {
        runOrPostpone {
            getFragmentStack()?.replace(ReportResultFragment.create(sendWriteOffDataResult))
        }
    }


    override fun openGoodsReasonsScreen(productInfo: ProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(WriteOffDetailsFragment.create(productInfo))
        }
    }

    override fun openSuccessPrintMessage() {
        openAlertScreen(context.getString(R.string.print_success))
    }

    override fun openDetectionSavedDataScreen() {
        runOrPostpone {
            getFragmentStack()?.push(DetectionSavedDataFragment())
        }
    }

    override fun openAlertGoodsNotForTaskScreen() {
        openInfoScreen(context.getString(R.string.goods_not_for_task))
    }


    override fun openNotPossibleSaveNegativeQuantityScreen() {
        openInfoScreen(message = context.getString(R.string.cannot_save_negative_quantity))
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

    override fun openAlertDoubleScanStamp() {
        openAlertScreen(
                message = context.getString(R.string.alert_double_scan_stamp),
                iconRes = R.drawable.ic_info_pink_80dp,
                textColor = ContextCompat.getColor(context, com.lenta.shared.R.color.color_text_dialogWarning),
                pageNumber = "97"
        )
    }

    override fun openNotAllComponentProcessedScreen(codeConfirmation: Int) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.not_all_component_processed),
                    codeConfirmForRight = codeConfirmation,
                    pageNumber = "95",
                    leftButtonDecorationInfo = ButtonDecorationInfo.back,
                    rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate)
            )
        }
    }

    override fun openStampsCountAlreadyScannedScreen() {
        openInfoScreen(
                message = context.getString(R.string.marks_comp_already_scanned)
        )
    }

    override fun openAlertNotValidFormatStamp() {
        openAlertScreen(
                message = context.getString(R.string.not_valid_format_stamp),
                iconRes = R.drawable.ic_warning_yellow_80dp,
                pageNumber = "96"
        )
    }

    override fun openFailDetectComponentForStampScreen() {
        openAlertScreen(
                message = context.getString(R.string.fail_detect_product_for_stamp),
                iconRes = R.drawable.ic_info_pink_80dp,
                textColor = ContextCompat.getColor(context, com.lenta.shared.R.color.color_text_dialogWarning),
                pageNumber = "97"
        )
    }

    override fun openLimitExceededScreen() {
        openAlertScreen(
                message = context.getString(R.string.limit_exceeded_alco_product),
                iconRes = R.drawable.ic_warning_yellow_80dp,
                pageNumber = "96",
                timeAutoExitInMillis = 2000
        )
    }

    override fun openConfirmationToBackNotEmptyStampsScreen(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.confirmation_back_with_scanned_stamps),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    pageNumber = "93",
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun openNotPossibleSaveWithoutReasonScreen() {
        openInfoScreen(message = context.getString(R.string.not_possible_save_without_reason))
    }

    override fun showWriteOffToProductionConfirmation(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(
                    AlertFragment.create(
                            message = context.getString(com.lenta.shared.R.string.writeoff_to_production_confirmation),
                            pageNumber = "95",
                            codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                            rightButtonDecorationInfo = ButtonDecorationInfo.nextAlternate
                    )
            )
        }
    }

    override fun showIncorrectMarkScanMode() {
        openAlertScreen(
                message = context.getString(R.string.incorrect_mark_scan_mode),
                iconRes = R.drawable.ic_warning_yellow_80dp,
                pageNumber = "96"
        )
    }

    override fun showProductInfoNotFound() {
        openAlertScreen(
                message = context.getString(R.string.incorrect_product_info),
                iconRes = R.drawable.ic_warning_yellow_80dp,
                pageNumber = "96"
        )
    }

}

interface IScreenNavigator : ICoreNavigator {

    fun openFirstScreen()
    fun openLoginScreen()
    fun openSelectMarketScreen()
    fun openFastDataLoadingScreen()
    fun openSelectionPersonnelNumberScreen(codeConfirmation: Int? = null, isScreenMainMenu: Boolean)
    fun openMainMenuScreen()
    fun openJobCardScreen()
    fun openLoadingTaskSettingsScreen()
    fun openGoodsListScreen()
    fun openGoodInfoScreen(productInfo: ProductInfo, quantity: Double = 0.0)
    fun openMarkedInfoScreen(productInfo: ProductInfo, quantity: Double = 0.0)
    fun openExciseAlcoScreen(productInfo: ProductInfo)
    fun openRemoveTaskConfirmationScreen(taskDescription: String, codeConfirmation: Int)
    fun openSendingReportsScreen(sendWriteOffDataResult: SendWriteOffDataResult)
    fun openSetsInfoScreen(productInfo: ProductInfo, quantity: Double)
    fun openGoodsReasonsScreen(productInfo: ProductInfo)
    fun openSuccessPrintMessage()
    fun openComponentSetScreen(productInfo: ProductInfo, componentItem: ComponentItem, targetTotalCount: Double)
    fun openDetectionSavedDataScreen()
    fun openRemoveLinesConfirmationScreen(taskDescription: String, count: Int, codeConfirmation: Int)
    fun openRemoveItemsFromSelectedCategory(count: Int, applyCallback: () -> Unit)
    fun openMatrixAlertScreen(matrixType: MatrixType, yesCallback: () -> Unit)
    fun openAlertGoodsNotForTaskScreen()
    fun openNotPossibleSaveNegativeQuantityScreen()
    fun openSelectTypeCodeScreen(codeConfirmationForSap: Int, codeConfirmationForBarCode: Int)
    fun openAlertDoubleScanStamp()
    fun openNotAllComponentProcessedScreen(codeConfirmation: Int)
    fun openStampsCountAlreadyScannedScreen()
    fun openAlertNotValidFormatStamp()
    fun openFailDetectComponentForStampScreen()
    fun openLimitExceededScreen()
    fun openNotPossibleSaveWithoutReasonScreen()
    fun openConfirmationToBackNotEmptyStampsScreen(callbackFunc: () -> Unit)
    fun showWriteOffToProductionConfirmation(nextCallback: () -> Unit)
    fun showIncorrectMarkScanMode()
    fun showProductInfoNotFound()

}