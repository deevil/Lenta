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
import com.lenta.bp9.features.change_datetime.ChangeDateTimeFragment
import com.lenta.bp9.features.change_datetime.ChangeDateTimeMode
import com.lenta.bp9.features.discrepancy_list.DiscrepancyListFragment
import com.lenta.bp9.features.goods_details.GoodsDetailsFragment
import com.lenta.bp9.features.goods_information.general.GoodsInfoFragment
import com.lenta.bp9.features.goods_information.non_excise_alco.NonExciseAlcoInfoFragment
import com.lenta.bp9.features.loading.tasks.*
import com.lenta.bp9.features.reject.RejectFragment
import com.lenta.bp9.features.revise.*
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.features.revise.invoice.InvoiceReviseFragment
import com.lenta.bp9.model.task.revise.ProductDocumentType
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.alert.AlertFragment
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

    override fun openLoadingStartReviseScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingStartReviseFragment())
        }
    }

    override fun openTaskReviseScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskReviseFragment())
        }
    }

    override fun openGoodsInfoScreen(productInfo: TaskProductInfo) {
        runOrPostpone {
            getFragmentStack()?.push(GoodsInfoFragment.create(productInfo))
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

    override fun openCheckingNotNeededAlert(callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.revise_not_needed),
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

    override fun openNonExciseAlcoInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(NonExciseAlcoInfoFragment())
        }
    }

    override fun openFinishReviseLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(LoadingFinishReviseFragment())
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
    fun openCheckingNotNeededAlert(callbackFunc: () -> Unit)
    fun openAlertWithoutConfirmation(message: String, callbackFunc: () -> Unit)
    fun openChangeDateTimeScreen(mode: ChangeDateTimeMode)
    fun openLoadingRegisterArrivalScreen()
    fun openLoadingStartReviseScreen()
    fun openTaskReviseScreen()
    fun openGoodsInfoScreen(productInfo: TaskProductInfo)
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
    fun openNonExciseAlcoInfoScreen()
    fun openFinishReviseLoadingScreen()
}