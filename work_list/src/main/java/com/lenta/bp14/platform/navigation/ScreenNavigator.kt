package com.lenta.bp14.platform.navigation

import android.content.Context
import com.lenta.bp14.R
import com.lenta.bp14.features.auth.AuthFragment
import com.lenta.bp14.features.barcode_detection.CoreScanBarCodeFragment
import com.lenta.bp14.features.check_list.ean_scanner.EanVideoScannerFragment
import com.lenta.bp14.features.check_list.goods_list.GoodsListClFragment
import com.lenta.bp14.features.work_list.good_info.GoodInfoWlFragment
import com.lenta.bp14.features.job_card.JobCardFragment
import com.lenta.bp14.features.list_of_differences.ListOfDifferencesFragment
import com.lenta.bp14.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp14.features.main_menu.MainMenuFragment
import com.lenta.bp14.features.not_exposed.goods_list.GoodsListNeFragment
import com.lenta.bp14.features.not_exposed.good_info.GoodInfoNeFragment
import com.lenta.bp14.features.price_check.good_info.GoodInfoPcFragment
import com.lenta.bp14.features.price_check.goods_list.GoodsListPcFragment
import com.lenta.bp14.features.price_check.price_scanner.PriceScannerFragment
import com.lenta.bp14.features.print_settings.PrintSettingsFragment
import com.lenta.bp14.features.report_result.ReportResultFragment
import com.lenta.bp14.features.select_market.SelectMarketFragment
import com.lenta.bp14.features.task_list.TaskListFragment
import com.lenta.bp14.features.task_list.search_filter.SearchFilterTlFragment
import com.lenta.bp14.features.work_list.good_details.GoodDetailsFragment
import com.lenta.bp14.features.work_list.expected_deliveries.ExpectedDeliveriesFragment
import com.lenta.bp14.features.work_list.goods_list.GoodsListWlFragment
import com.lenta.bp14.features.work_list.good_sales.GoodSalesFragment
import com.lenta.bp14.features.work_list.search_filter.SearchFilterWlFragment
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
            getFragmentStack()?.push(SelectMarketFragment())
        }
    }

    override fun openMainMenuScreen() {
        runOrPostpone {
            getFragmentStack()?.push(MainMenuFragment())
        }
    }

    override fun openFastDataLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FastDataLoadingFragment())
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

    override fun openJobCardScreen(taskNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(JobCardFragment.create(taskNumber = taskNumber))
        }
    }

    override fun openGoodsListClScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListClFragment())
        }
    }

    override fun openListOfDifferencesScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ListOfDifferencesFragment())
        }
    }

    override fun openReportResultScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ReportResultFragment())
        }
    }

    override fun openPrintSettingsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(PrintSettingsFragment())
        }
    }

    override fun openGoodDetailsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodDetailsFragment())
        }
    }

    override fun openGoodInfoWlScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoWlFragment())
        }
    }

    override fun openGoodsListWlScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListWlFragment())
        }
    }

    override fun openGoodInfoPcScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoPcFragment())
        }
    }

    override fun openGoodsListPcScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListPcFragment())
        }
    }

    override fun openExpectedDeliveriesScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ExpectedDeliveriesFragment())
        }
    }

    override fun openSearchFilterWlScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SearchFilterWlFragment())
        }
    }

    override fun openSearchFilterTlScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SearchFilterTlFragment())
        }
    }

    override fun openGoodSalesScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodSalesFragment())
        }
    }

    override fun openGoodsListNeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListNeFragment())
        }
    }

    override fun openGoodInfoNeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoNeFragment())
        }
    }

    override fun openTestScanBarcodeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(CoreScanBarCodeFragment())
        }
    }

    override fun openScanPriceScreen() {
        runOrPostpone {
            getFragmentStack()?.push(PriceScannerFragment())
        }
    }

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack


    // Информационные экраны
    override fun showConfirmPriceTagsPrinting(priceTagNumber: Int, confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.confirm_price_tags_printing, priceTagNumber),
                    pageNumber = "10",
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm))
        }
    }

    override fun showMakeSureYellowPaperInstalled(printerName: String, numberOfCopy: Int, confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.make_sure_yellow_paper_installed, printerName, numberOfCopy),
                    pageNumber = "10.1",
                    iconRes = R.drawable.ic_price_yellow_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm))
        }
    }

    override fun showMakeSureRedPaperInstalled(printerName: String, numberOfCopy: Int, confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.make_sure_red_paper_installed, printerName, numberOfCopy),
                    pageNumber = "10.2",
                    iconRes = R.drawable.ic_price_red_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm))
        }
    }

    override fun showPriceTagsSubmitted(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.price_tags_submitted),
                    pageNumber = "11",
                    iconRes = R.drawable.ic_done_green_80dp,
                    isVisibleLeftButton = false,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next))
        }
    }

    override fun showSetTaskToStatusCalculated(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.set_task_to_status_calculated),
                    pageNumber = "24",
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun showRawGoodsRemainedInTask(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.raw_goods_remained_in_task),
                    pageNumber = "37",
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun showRawGoodsRemainedInTask(goodName: String, yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.print_price_tag_for_good, goodName),
                    pageNumber = "43",
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.backNo,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun showUnsavedDataFoundOnDevice(deleteCallback: () -> Unit, goOverCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.unsaved_data_found_on_device),
                    pageNumber = "92",
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForButton3 = backFragmentResultHelper.setFuncForResult(deleteCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(goOverCallback),
                    buttonDecorationInfo3 = ButtonDecorationInfo.delete,
                    rightButtonDecorationInfo = ButtonDecorationInfo.goOver))
        }
    }

    override fun showUnsavedTaskFoundOnDevice(deleteCallback: () -> Unit, goOverCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.unsaved_task_found_on_device),
                    pageNumber = "92",
                    iconRes = R.drawable.ic_question_80dp,
                    isVisibleLeftButton = false,
                    codeConfirmForButton3 = backFragmentResultHelper.setFuncForResult(deleteCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(goOverCallback),
                    buttonDecorationInfo3 = ButtonDecorationInfo.delete,
                    rightButtonDecorationInfo = ButtonDecorationInfo.goOver))
        }
    }

    override fun showGoodIsNotPartOfTask() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.good_is_not_part_of_task),
                    pageNumber = "109",
                    iconRes = R.drawable.is_warning_red_80dp))
        }
    }

    override fun showScannedGoodNotListedInLenta(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.scanned_good_not_listed_in_lenta),
                    pageNumber = "112",
                    iconRes = R.drawable.is_warning_yellow_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next))
        }
    }

    override fun showScannedGoodNotListedInTk(marketNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.scanned_good_not_listed_in_tk, marketNumber),
                    pageNumber = "114",
                    iconRes = R.drawable.is_warning_red_80dp))
        }
    }

    override fun showScannedGoodAlreadyAddedToTask(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.scanned_good_already_added_to_task),
                    pageNumber = "116",
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun showScannedGoodNotListedInTk() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.number_of_positions_exceeded_in_task),
                    pageNumber = "118",
                    iconRes = R.drawable.is_warning_red_80dp))
        }
    }

    override fun showNoNetworkToSaveTask(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.no_network_to_save_task),
                    pageNumber = "119",
                    iconRes = R.drawable.is_warning_yellow_80dp,
                    isVisibleLeftButton = false,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next))
        }
    }

    override fun openConfirmationExitTask(taskName: String, callbackFunc: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.confirmation_delete_task, taskName),
                    iconRes = R.drawable.ic_delete_red_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(callbackFunc),
                    pageNumber = "94",
                    leftButtonDecorationInfo = ButtonDecorationInfo.back,
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm
            )
            )
        }
    }

    override fun showGoodNotFound() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.good_not_found_in_database),
                    pageNumber = "100",
                    timeAutoExitInMillis = 2000))
        }
    }

    override fun showTwelveCharactersEntered(sapCallback: () -> Unit, barCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.twelve_characters_entered),
                    pageNumber = "40",
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(sapCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(barCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.sap,
                    rightButtonDecorationInfo = ButtonDecorationInfo.barcode))
        }
    }

    override fun openVideoScanProductScreen() {
        runOrPostpone {
            getFragmentStack()?.push(EanVideoScannerFragment())
        }
    }

}

interface IScreenNavigator : ICoreNavigator {

    fun openFirstScreen()
    fun openSelectMarketScreen()
    fun openMainMenuScreen()
    fun openLoginScreen()
    fun openFastDataLoadingScreen()
    fun openTaskListScreen()
    fun openJobCardScreen(taskNumber: String)
    fun openGoodsListClScreen()
    fun openListOfDifferencesScreen()
    fun openReportResultScreen()
    fun openPrintSettingsScreen()
    fun openGoodDetailsScreen()
    fun openGoodInfoWlScreen()
    fun openGoodsListWlScreen()
    fun openGoodInfoPcScreen()
    fun openGoodsListPcScreen()
    fun openSearchFilterWlScreen()
    fun openSearchFilterTlScreen()
    fun openExpectedDeliveriesScreen()
    fun openGoodSalesScreen()
    fun openGoodsListNeScreen()
    fun openGoodInfoNeScreen()

    fun showConfirmPriceTagsPrinting(priceTagNumber: Int, confirmCallback: () -> Unit)
    fun showMakeSureYellowPaperInstalled(printerName: String, numberOfCopy: Int, confirmCallback: () -> Unit)
    fun showMakeSureRedPaperInstalled(printerName: String, numberOfCopy: Int, confirmCallback: () -> Unit)
    fun showPriceTagsSubmitted(nextCallback: () -> Unit)
    fun showSetTaskToStatusCalculated(yesCallback: () -> Unit)
    fun showRawGoodsRemainedInTask(yesCallback: () -> Unit)
    fun showRawGoodsRemainedInTask(goodName: String, yesCallback: () -> Unit)
    fun showUnsavedDataFoundOnDevice(deleteCallback: () -> Unit, goOverCallback: () -> Unit)
    fun showUnsavedTaskFoundOnDevice(deleteCallback: () -> Unit, goOverCallback: () -> Unit)
    fun showGoodIsNotPartOfTask()
    fun showScannedGoodNotListedInLenta(nextCallback: () -> Unit)
    fun showScannedGoodNotListedInTk(marketNumber: String)
    fun showScannedGoodAlreadyAddedToTask(yesCallback: () -> Unit)
    fun showScannedGoodNotListedInTk()
    fun showNoNetworkToSaveTask(nextCallback: () -> Unit)
    fun showGoodNotFound()
    fun showTwelveCharactersEntered(sapCallback: () -> Unit, barCallback: () -> Unit)

    fun openTestScanBarcodeScreen()
    fun openScanPriceScreen()
    fun openConfirmationExitTask(taskName: String, callbackFunc: () -> Unit)
    fun openVideoScanProductScreen()
}