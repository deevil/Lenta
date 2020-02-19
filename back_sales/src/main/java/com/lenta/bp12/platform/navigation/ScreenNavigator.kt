package com.lenta.bp12.platform.navigation

import android.content.Context
import com.lenta.bp12.R
import com.lenta.bp12.features.auth.AuthFragment
import com.lenta.bp12.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp12.features.main_menu.MainMenuFragment
import com.lenta.bp12.features.select_market.SelectMarketFragment
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.progress.IProgressUseCaseInformator
import javax.inject.Inject

class ScreenNavigator @Inject constructor(
        private val context: Context,
        private val coreNavigator: ICoreNavigator,
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator,
        private val progressUseCaseInformator: IProgressUseCaseInformator
) : IScreenNavigator, ICoreNavigator by coreNavigator {

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

    override fun openFirstScreen() {
        if (authenticator.isAuthorized()) {
            openMainMenuScreen()
        } else {
            openLoginScreen()
        }
    }


    // Основные экраны
    override fun openLoginScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AuthFragment())
        }
    }

    override fun openFastDataLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FastDataLoadingFragment())
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


    // Информационные экраны
    override fun showUnsentDataFoundOnDevice(deleteCallback: () -> Unit, goOverCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "89",
                    message = context.getString(R.string.unsent_data_found_on_device),
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForButton3 = backFragmentResultHelper.setFuncForResult(deleteCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(goOverCallback),
                    buttonDecorationInfo3 = ButtonDecorationInfo.delete,
                    rightButtonDecorationInfo = ButtonDecorationInfo.goOver
            ))
        }
    }

    override fun showTwelveCharactersEntered(sapCallback: () -> Unit, barCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "90",
                    message = context.getString(R.string.twelve_characters_entered),
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(sapCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(barCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.sap,
                    rightButtonDecorationInfo = ButtonDecorationInfo.barcode
            ))
        }
    }

    override fun showUnsavedDataWillBeLost(proceedCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "92",
                    message = context.getString(R.string.unsaved_data_will_be_lost),
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(proceedCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.proceed
            ))
        }
    }

    override fun showMakeTaskCountedAndClose(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "94",
                    message = context.getString(R.string.make_task_counted_and_close),
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes
            ))
        }
    }

    override fun showTaskUnsentDataWillBeDeleted(taskName: String, applyCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "95",
                    message = context.getString(R.string.task_unsent_data_will_be_deleted, taskName),
                    iconRes = R.drawable.ic_delete_red_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(applyCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.apply
            ))
        }
    }

    override fun showScannedMarkBelongsToProduct(productName: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "98",
                    message = context.getString(R.string.scanned_mark_belongs_to_product, productName),
                    iconRes = R.drawable.is_warning_red_80dp
            ))
        }
    }

    override fun showForExciseGoodNeedScanFirstMark() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "96",
                    message = context.getString(R.string.for_excise_good_need_scan_first_mark),
                    iconRes = R.drawable.ic_info_green_80dp,
                    timeAutoExitInMillis = 2000
            ))
        }
    }

    override fun showRawGoodsRemainedInTask(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "36",
                    message = context.getString(R.string.raw_goods_remained_in_task),
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes
            ))
        }
    }

    override fun showBoxWasLastScanned(afterShowCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "96",
                    message = context.getString(R.string.box_was_last_scanned),
                    iconRes = R.drawable.is_warning_red_80dp,
                    codeConfirmForExit = backFragmentResultHelper.setFuncForResult(afterShowCallback),
                    timeAutoExitInMillis = 2000
            ))
        }
    }

    override fun showDoYouReallyWantSetZeroQuantity(yesCallback: () -> Unit, counted: Int) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "65",
                    message = context.getString(R.string.do_you_really_want_set_zero_quantity, counted),
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes
            )
            )
        }
    }




}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openFastDataLoadingScreen()
    fun openSelectMarketScreen()
    fun openMainMenuScreen()

    fun showUnsentDataFoundOnDevice(deleteCallback: () -> Unit, goOverCallback: () -> Unit)
    fun showTwelveCharactersEntered(sapCallback: () -> Unit, barCallback: () -> Unit)
    fun showUnsavedDataWillBeLost(proceedCallback: () -> Unit)
    fun showMakeTaskCountedAndClose(yesCallback: () -> Unit)
    fun showTaskUnsentDataWillBeDeleted(taskName: String, applyCallback: () -> Unit)
    fun showScannedMarkBelongsToProduct(productName: String)
    fun showForExciseGoodNeedScanFirstMark()
    fun showRawGoodsRemainedInTask(yesCallback: () -> Unit)
    fun showBoxWasLastScanned(afterShowCallback: () -> Unit)
    fun showDoYouReallyWantSetZeroQuantity(yesCallback: () -> Unit, counted: Int)
}