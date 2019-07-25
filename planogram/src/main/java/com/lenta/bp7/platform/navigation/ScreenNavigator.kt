package com.lenta.bp7.platform.navigation

import android.content.Context
import com.lenta.bp7.R
import com.lenta.bp7.features.auth.AuthFragment
import com.lenta.bp7.features.check_type.CheckTypeFragment
import com.lenta.bp7.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp7.features.option_info.OptionInfoFragment
import com.lenta.bp7.features.code.CodeFragment
import com.lenta.bp7.features.good_info.GoodInfoFragment
import com.lenta.bp7.features.good_info_facing.GoodInfoFacingFragment
import com.lenta.bp7.features.good_list.GoodListFragment
import com.lenta.bp7.features.segment_list.SegmentListFragment
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

    override fun openMainMenuScreen() {
        openNotImplementedScreenAlert("Главное меню")
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

    override fun openCheckTypeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(CheckTypeFragment())
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


    override fun showShelfDataWillNotBeSaved(segment: String, shelf: String, confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.shelf_data_will_not_be_saved, segment, shelf),
                    pageNumber = "44",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm))
        }
    }

    override fun showSaveShelfScanResults(segment: String, shelf: String, yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.save_shelf_scan_results, segment, shelf),
                    pageNumber = "21",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun showSaveSegmentScanResults(segment: String, yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.save_segment_scan_results, segment),
                    pageNumber = "23",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun showIsEmptyPlaceDecoratedCorrectly(sap: String, name: String, segment: String, shelf: String, noCallback: () -> Unit, yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.is_empty_place_decorated_correctly, sap, name, segment, shelf),
                    pageNumber = "16",
                    codeConfirmForButton4 = backFragmentResultHelper.setFuncForResult(noCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    buttonDecorationInfo4 = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes))
        }
    }

    override fun showSegmentStarted(segment: String, facings: Boolean, afterShowCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(if (facings) R.string.segment_started_with_facings else R.string.segment_started_without_facings, segment),
                    pageNumber = "9",
                    codeConfirmForExit = backFragmentResultHelper.setFuncForResult(afterShowCallback),
                    timeAutoExitInMillis = 3000))
        }
    }

    override fun showShelfStarted(segment: String, shelf: String, afterShowCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.shelf_started, segment, shelf),
                    pageNumber = "11",
                    codeConfirmForExit = backFragmentResultHelper.setFuncForResult(afterShowCallback),
                    timeAutoExitInMillis = 3000))
        }
    }

    override fun showDeleteDataOnSegment(store: String, segment: String, deleteCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.delete_data_on_segment, store, segment),
                    pageNumber = "53",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(deleteCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.delete))
        }
    }

    override fun showNoShelvesInSegmentToSave(segment: String, confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.no_shelves_in_segment_to_save, segment),
                    pageNumber = "48",
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm))
        }
    }
}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openMainMenuScreen()
    fun openSelectMarketScreen()
    fun openFastDataLoadingScreen()
    fun openCheckTypeScreen()
    fun openCodeScreen()
    fun openOptionScreen()
    fun openSegmentListScreen()
    fun openShelfListScreen()
    fun openGoodListScreen()
    fun openGoodInfoFacingScreen()
    fun openGoodInfoScreen()

    fun showShelfDataWillNotBeSaved(segment: String, shelf: String, confirmCallback: () -> Unit)
    fun showSaveShelfScanResults(segment: String, shelf: String, yesCallback: () -> Unit)
    fun showSaveSegmentScanResults(segment: String, yesCallback: () -> Unit)
    fun showIsEmptyPlaceDecoratedCorrectly(sap: String, name: String, segment: String, shelf: String, noCallback: () -> Unit, yesCallback: () -> Unit)
    fun showSegmentStarted(segment: String, facings: Boolean, afterShowCallback: () -> Unit)
    fun showShelfStarted(segment: String, shelf: String, afterShowCallback: () -> Unit)
    fun showDeleteDataOnSegment(store: String, segment: String, deleteCallback: () -> Unit)
    fun showNoShelvesInSegmentToSave(segment: String, confirmCallback: () -> Unit)

}