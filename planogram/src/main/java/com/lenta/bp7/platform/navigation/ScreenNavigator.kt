package com.lenta.bp7.platform.navigation

import android.content.Context
import com.lenta.bp7.R
import com.lenta.bp7.features.auth.AuthFragment
import com.lenta.bp7.features.code.CodeFragment
import com.lenta.bp7.features.good_info.GoodInfoFragment
import com.lenta.bp7.features.good_info_facing.GoodInfoFacingFragment
import com.lenta.bp7.features.good_list.GoodListFragment
import com.lenta.bp7.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp7.features.option_info.OptionInfoFragment
import com.lenta.bp7.features.segment_list.SegmentListFragment
import com.lenta.bp7.features.select_check_type.SelectCheckTypeFragment
import com.lenta.bp7.features.select_market.SelectMarketFragment
import com.lenta.bp7.features.shelf_list.ShelfListFragment
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

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

    override fun closeAllScreen() {
        runOrPostpone {
            getFragmentStack()?.popAll()
        }
    }

    override fun openFirstScreen() {
        openLoginScreen()
    }

    override fun openLoginScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AuthFragment())
        }
    }

    override fun openSelectMarketScreen() {
        runOrPostpone {
            getFragmentStack()?.replace(SelectMarketFragment())
        }
    }

    override fun openFastDataLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FastDataLoadingFragment())
        }
    }

    override fun openSelectCheckTypeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SelectCheckTypeFragment())
        }
    }

    override fun openCodeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(CodeFragment())
        }
    }

    override fun openOptionScreen() {
        runOrPostpone {
            getFragmentStack()?.push(OptionInfoFragment())
        }
    }

    override fun openSegmentListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SegmentListFragment())
        }
    }

    override fun openShelfListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ShelfListFragment())
        }
    }

    override fun openGoodListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodListFragment())
        }
    }

    override fun openGoodInfoFacingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoFacingFragment())
        }
    }

    override fun openGoodInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoFragment())
        }
    }


    override fun showShelfDataWillNotBeSaved(segmentNumber: String, shelfNumber: String, confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.shelf_data_will_not_be_saved, segmentNumber, shelfNumber),
                    pageNumber = "44",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm))
        }
    }

    override fun showSaveShelfScanResults(segmentNumber: String, shelfNumber: String, yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.save_shelf_scan_results, segmentNumber, shelfNumber),
                    pageNumber = "21",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun showSaveSegmentScanResults(segmentNumber: String, yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.save_segment_scan_results, segmentNumber),
                    pageNumber = "23",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun showIsEmptyPlaceDecoratedCorrectly(material: String, name: String, segmentNumber: String, shelfNumber: String, noCallback: () -> Unit, yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.is_empty_place_decorated_correctly, material, name, segmentNumber, shelfNumber),
                    pageNumber = "16",
                    codeConfirmForButton4 = backFragmentResultHelper.setFuncForResult(noCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    buttonDecorationInfo4 = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun showSegmentStarted(segmentNumber: String, isFacings: Boolean, afterShowCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(if (isFacings) R.string.segment_started_with_facings else R.string.segment_started_without_facings, segmentNumber),
                    pageNumber = "9",
                    codeConfirmForExit = backFragmentResultHelper.setFuncForResult(afterShowCallback),
                    timeAutoExitInMillis = 2000))
        }
    }

    override fun showShelfStarted(segmentNumber: String, shelfNumber: String, afterShowCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.shelf_started, segmentNumber, shelfNumber),
                    pageNumber = "11",
                    codeConfirmForExit = backFragmentResultHelper.setFuncForResult(afterShowCallback),
                    timeAutoExitInMillis = 2000))
        }
    }

    override fun showDeleteDataOnSegment(storeNumber: String, segmentNumber: String, deleteCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.delete_data_on_segment, storeNumber, segmentNumber),
                    pageNumber = "53",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(deleteCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.delete))
        }
    }

    override fun showNoShelvesInSegmentToSave(segmentNumber: String, confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.no_shelves_in_segment_to_save, segmentNumber),
                    pageNumber = "48",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm))
        }
    }

    override fun showIncompleteSegmentDetected(goOverCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.incomplete_segment_detected),
                    pageNumber = "72",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(goOverCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.goOver))
        }
    }

    override fun showUnsavedSelfControlDataDetected(goOverCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.unsaved_self_control_data_detected),
                    pageNumber = "70",
                    description = context.getString(R.string.unfinished_data),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(goOverCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.goOver))
        }
    }

    override fun showUnsavedExternalAuditDataDetected(goOverCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.unsaved_external_audit_data_detected),
                    pageNumber = "4",
                    description = context.getString(R.string.unfinished_data),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(goOverCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.goOver))
        }
    }

    override fun showUnknownGoodBarcode(barCode: String, yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.unknown_good_barcode, barCode),
                    pageNumber = "41",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun showShelfIsDeleted(reviewCallback: () -> Unit, createCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.shelf_is_deleted),
                    pageNumber = "46",
                    codeConfirmForButton4 = backFragmentResultHelper.setFuncForResult(reviewCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(createCallback),
                    buttonDecorationInfo4 = ButtonDecorationInfo.review,
                    rightButtonDecorationInfo = ButtonDecorationInfo.create))
        }
    }

    override fun showDeleteShelfData(shelfNumbers: String, deleteCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.delete_shelf_data, shelfNumbers),
                    pageNumber = "46.1",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(deleteCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.delete))
        }
    }

    override fun showSegmentIsDeleted(reviewCallback: () -> Unit, createCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.segment_is_deleted),
                    pageNumber = "46",
                    codeConfirmForButton4 = backFragmentResultHelper.setFuncForResult(reviewCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(createCallback),
                    buttonDecorationInfo4 = ButtonDecorationInfo.review,
                    rightButtonDecorationInfo = ButtonDecorationInfo.create))
        }
    }

    override fun showSuccessfullySavedToLua(afterShowCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.successfully_saved_to_lua),
                    pageNumber = "24",
                    iconRes = R.drawable.ic_done_green_80dp,
                    codeConfirmForExit = backFragmentResultHelper.setFuncForResult(afterShowCallback),
                    timeAutoExitInMillis = 2000))
        }
    }

    override fun showErrorSavingToLua(afterShowCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.error_saving_to_lua),
                    pageNumber = "61",
                    iconRes = R.drawable.ic_info_pink_80dp,
                    codeConfirmForExit = backFragmentResultHelper.setFuncForResult(afterShowCallback),
                    timeAutoExitInMillis = 2000))
        }
    }

    override fun showLuaSystemUnavailable(exitCallback: () -> Unit, nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.lua_system_unavailable),
                    pageNumber = "59",
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(exitCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.exit,
                    rightButtonDecorationInfo = ButtonDecorationInfo.next))
        }
    }

    override fun showDoYouReallyWantToLeave(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.do_you_really_want_to_leave),
                    pageNumber = "64",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next))
        }
    }

    override fun showUnsentDataDetected(exitToAppCallback: () -> Unit, goOverCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.unsent_data_detected),
                    pageNumber = "65",
                    codeConfirmForButton4 = backFragmentResultHelper.setFuncForResult(exitToAppCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(goOverCallback),
                    buttonDecorationInfo4 = ButtonDecorationInfo.exitToApp,
                    rightButtonDecorationInfo = ButtonDecorationInfo.goOver))
        }
    }

    override fun showGoodNotFound() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.good_not_found_in_database),
                    pageNumber = "100",
                    timeAutoExitInMillis = 2000))
        }
    }
}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openSelectMarketScreen()
    fun openFastDataLoadingScreen()
    fun openSelectCheckTypeScreen()
    fun openCodeScreen()
    fun openOptionScreen()
    fun openSegmentListScreen()
    fun openShelfListScreen()
    fun openGoodListScreen()
    fun openGoodInfoFacingScreen()
    fun openGoodInfoScreen()

    fun showShelfDataWillNotBeSaved(segmentNumber: String, shelfNumber: String, confirmCallback: () -> Unit)
    fun showSaveShelfScanResults(segmentNumber: String, shelfNumber: String, yesCallback: () -> Unit)
    fun showSaveSegmentScanResults(segmentNumber: String, yesCallback: () -> Unit)
    fun showIsEmptyPlaceDecoratedCorrectly(material: String, name: String, segmentNumber: String, shelfNumber: String, noCallback: () -> Unit, yesCallback: () -> Unit)
    fun showSegmentStarted(segmentNumber: String, isFacings: Boolean, afterShowCallback: () -> Unit)
    fun showShelfStarted(segmentNumber: String, shelfNumber: String, afterShowCallback: () -> Unit)
    fun showDeleteDataOnSegment(storeNumber: String, segmentNumber: String, deleteCallback: () -> Unit)
    fun showNoShelvesInSegmentToSave(segmentNumber: String, confirmCallback: () -> Unit)
    fun showIncompleteSegmentDetected(goOverCallback: () -> Unit)
    fun showUnsavedSelfControlDataDetected(goOverCallback: () -> Unit)
    fun showUnsavedExternalAuditDataDetected(goOverCallback: () -> Unit)
    fun showUnknownGoodBarcode(barCode: String, yesCallback: () -> Unit)
    fun showShelfIsDeleted(reviewCallback: () -> Unit, createCallback: () -> Unit)
    fun showDeleteShelfData(shelfNumbers: String, deleteCallback: () -> Unit)
    fun showSegmentIsDeleted(reviewCallback: () -> Unit, createCallback: () -> Unit)
    fun showSuccessfullySavedToLua(afterShowCallback: () -> Unit)
    fun showErrorSavingToLua(afterShowCallback: () -> Unit)
    fun showLuaSystemUnavailable(exitCallback: () -> Unit, nextCallback: () -> Unit)
    fun showDoYouReallyWantToLeave(nextCallback: () -> Unit)
    fun showUnsentDataDetected(exitToAppCallback: () -> Unit, goOverCallback: () -> Unit)
    fun showGoodNotFound()

}