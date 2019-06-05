package com.lenta.bp10.platform.navigation

import android.content.Context
import com.lenta.bp10.R
import com.lenta.bp10.exception.IWriteOffFailureInterpretator
import com.lenta.bp10.features.auth.AuthFragment
import com.lenta.bp10.features.detection_saved_data.DetectionSavedDataFragment
import com.lenta.bp10.features.good_information.general.GoodInfoFragment
import com.lenta.bp10.features.good_information.sets.ComponentItem
import com.lenta.bp10.features.good_information.sets.SetsFragment
import com.lenta.bp10.features.good_information.sets.component.ComponentFragment
import com.lenta.bp10.features.goods_list.GoodsListFragment
import com.lenta.bp10.features.job_card.JobCardFragment
import com.lenta.bp10.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp10.features.loading.tasks_settings.LoadingTaskSettingsFragment
import com.lenta.bp10.features.main_menu.MainMenuFragment
import com.lenta.bp10.features.matrix_info.MatrixInfoFragment
import com.lenta.bp10.features.report_result.ReportResultFragment
import com.lenta.bp10.features.section_info.SectionInfoFragment
import com.lenta.bp10.features.select_market.SelectMarketFragment
import com.lenta.bp10.features.select_personnel_number.SelectPersonnelNumberFragment
import com.lenta.bp10.features.write_off_details.WriteOffDetailsFragment
import com.lenta.bp10.requests.network.WriteOffReportResponse
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.features.printer_change.PrinterChangeFragment
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
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
        private val failureInterpreter: IWriteOffFailureInterpretator,
        private val progressUseCaseInformator: IProgressUseCaseInformator
) : IScreenNavigator, ICoreNavigator by coreNavigator {

    override fun openAlertScreen(failure: Failure, pageNumber: String) {
        openAlertScreen(failureInterpreter.getFailureDescription(failure))
    }


    override fun openSelectMarketScreen() {
        runOrPostpone {
            getFragmentStack()?.replace(SelectMarketFragment())
        }
    }

    override fun openFirstScreen() {
        if (authenticator.isAuthorized()) {
            openMainMenuScreen()
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

    override fun openSelectionPersonnelNumberScreen(codeConfirmation: Int?) {
        runOrPostpone {
            if (codeConfirmation == null){
                getFragmentStack()?.replace(SelectPersonnelNumberFragment())
            } else {
                getFragmentStack()?.push(SelectPersonnelNumberFragment.create(codeConfirmation =  codeConfirmation))
            }
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

    override fun openGoodInfoScreen(productInfo: ProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoFragment.create(productInfo))
        }
    }

    override fun openSetsInfoScreen(productInfo: ProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(SetsFragment.create(productInfo))
        }
    }

    override fun openPrinterChangeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(PrinterChangeFragment())
        }
    }

    override fun openComponentSetScreen(productInfo: ProductInfo, componentItem: ComponentItem) {
        runOrPostpone {
            getFragmentStack()?.push(ComponentFragment.create(productInfo, componentItem))
        }
    }

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

    override fun openEanInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.ean_info),
                    iconRes = R.drawable.ic_scan_barcode))
        }
    }

    override fun openESInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.es_info),
                    iconRes = R.drawable.is_scan_barcode_es))
        }
    }

    override fun openRemoveTaskConfirmationScreen(taskDescription: String, codeConfirmation: Int) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.remove_task_confirmation, taskDescription),
                    iconRes = R.drawable.ic_delete_red_80dp, codeConfirm = codeConfirmation, pageNumber = "10/88"))
        }
    }

    override fun openMatrixAlertScreen(matrixType: MatrixType, codeConfirmation: Int) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(if (matrixType == MatrixType.Deleted) R.string.allert_deleted_matrix_message else R.string.allert_unknown_matrix_message),
                    iconRes = 0,
                    codeConfirm = codeConfirmation,
                    pageNumber = "10/94",
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes)
            )
        }
    }

    override fun openRemoveLinesConfirmationScreen(taskDescription: String, count: Int, codeConfirmation: Int) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.remove_lines_confirmation, count),
                    iconRes = R.drawable.ic_delete_red_80dp, codeConfirm = codeConfirmation, pageNumber = "10/89"))
        }
    }

    override fun openSendingReportsScreen(writeOffReportResponse: WriteOffReportResponse) {
        runOrPostpone {
            getFragmentStack()?.replace(ReportResultFragment.create(writeOffReportResponse))
        }
    }

    override fun closeAllScreen() {
        runOrPostpone {
            getFragmentStack()?.popAll()
        }
    }

    override fun openMatrixInfoScreen(matrixType: MatrixType) {
        runOrPostpone {
            getFragmentStack()?.push(MatrixInfoFragment.create(matrixType))
        }
    }

    override fun openSectionInfoScreen(section: Int) {
        runOrPostpone {
            getFragmentStack()?.push(SectionInfoFragment.create(sectionNumber = "$section"))
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


}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openSelectMarketScreen()
    fun openFastDataLoadingScreen()
    fun openSelectionPersonnelNumberScreen(codeConfirmation: Int?)
    fun openMainMenuScreen()
    fun openJobCardScreen()
    fun openLoadingTaskSettingsScreen()
    fun openGoodsListScreen()
    fun openGoodInfoScreen(productInfo: ProductInfo)
    fun openEanInfoScreen()
    fun openESInfoScreen()
    fun openRemoveTaskConfirmationScreen(taskDescription: String, codeConfirmation: Int)
    fun openSendingReportsScreen(writeOffReportResponse: WriteOffReportResponse)
    fun closeAllScreen()
    fun openSetsInfoScreen(productInfo: ProductInfo)
    fun openMatrixInfoScreen(matrixType: MatrixType)
    fun openSectionInfoScreen(section: Int)
    fun openGoodsReasonsScreen(productInfo: ProductInfo)
    fun openSuccessPrintMessage()
    fun openComponentSetScreen(productInfo: ProductInfo, componentItem: ComponentItem)
    fun openDetectionSavedDataScreen()
    fun openRemoveLinesConfirmationScreen(taskDescription: String, count: Int, codeConfirmation: Int)
    fun openMatrixAlertScreen(matrixType: MatrixType, codeConfirmation: Int)
}