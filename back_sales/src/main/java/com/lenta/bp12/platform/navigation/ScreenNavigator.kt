package com.lenta.bp12.platform.navigation

import android.content.Context
import com.lenta.bp12.R
import com.lenta.bp12.features.add_supplier.AddSupplierFragment
import com.lenta.bp12.features.auth.AuthFragment
import com.lenta.bp12.features.basket_good_list.BasketGoodListFragment
import com.lenta.bp12.features.basket_properties.BasketPropertiesFragment
import com.lenta.bp12.features.discrepancy_list.DiscrepancyListFragment
import com.lenta.bp12.features.enter_employee_number.EnterEmployeeNumberFragment
import com.lenta.bp12.features.good_details.GoodDetailsFragment
import com.lenta.bp12.features.good_info.GoodInfoFragment
import com.lenta.bp12.features.good_list.GoodListFragment
import com.lenta.bp12.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp12.features.main_menu.MainMenuFragment
import com.lenta.bp12.features.save_data.SaveDataFragment
import com.lenta.bp12.features.select_market.SelectMarketFragment
import com.lenta.bp12.features.task_card.TaskCardFragment
import com.lenta.bp12.features.task_composition.TaskCompositionFragment
import com.lenta.bp12.features.task_list.TaskListFragment
import com.lenta.bp12.features.task_search.TaskSearchFragment
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


    // Базовые экраны
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

    override fun openEnterEmployeeNumberScreen() {
        runOrPostpone {
            getFragmentStack()?.push(EnterEmployeeNumberFragment())
        }
    }

    override fun openMainMenuScreen() {
        runOrPostpone {
            getFragmentStack()?.push(MainMenuFragment())
        }
    }


    // Основные экраны
    override fun openTaskCompositionScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskCompositionFragment())
        }
    }

    override fun openBasketGoodListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(BasketGoodListFragment())
        }
    }

    override fun openGoodDetailsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodDetailsFragment())
        }
    }

    override fun openSaveDataScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SaveDataFragment())
        }
    }

    override fun openTaskListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskListFragment())
        }
    }

    override fun openBasketPropertiesScreen() {
        runOrPostpone {
            getFragmentStack()?.push(BasketPropertiesFragment())
        }
    }

    override fun openDiscrepancyListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(DiscrepancyListFragment())
        }
    }

    override fun openGoodInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoFragment())
        }
    }

    override fun openGoodListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodListFragment())
        }
    }

    override fun openTaskCardScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskCardFragment())
        }
    }

    override fun openTaskSearchScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskSearchFragment())
        }
    }

    override fun openAddSupplierScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AddSupplierFragment())
        }
    }


    // Информационные экраны
    override fun showUnsentDataFoundOnDevice(deleteCallback: () -> Unit, goOverCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "5",
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
                    pageNumber = "68",
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
                    pageNumber = "19",
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
                    pageNumber = "75.1",
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
                    pageNumber = "85",
                    message = context.getString(R.string.scanned_mark_belongs_to_product, productName),
                    iconRes = R.drawable.is_warning_red_80dp
            ))
        }
    }

    override fun showForExciseGoodNeedScanFirstMark() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "93",
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
                    pageNumber = "59",
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
            ))
        }
    }

    override fun showProductDoesNotMatchTaskSettings() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "73",
                    message = context.getString(R.string.product_does_not_match_task_settings),
                    iconRes = R.drawable.is_warning_red_80dp
            ))
        }
    }

    override fun openScannedMarkIsNotOnBalanceInCurrentStore(proceedCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "84",
                    message = context.getString(R.string.scanned_mark_is_not_on_balance_in_current_store),
                    iconRes = R.drawable.is_warning_red_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(proceedCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.cancelBack,
                    rightButtonDecorationInfo = ButtonDecorationInfo.proceed
            ))
        }
    }

    override fun showScannedBoxIsNotWhole() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "89",
                    message = context.getString(R.string.scanned_box_is_not_whole),
                    iconRes = R.drawable.is_warning_red_80dp
            ))
        }
    }

    override fun showMarksInBoxAreNotOnBalanceInCurrentStore() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "91",
                    message = context.getString(R.string.marks_in_box_are_not_on_balance_in_current_store),
                    iconRes = R.drawable.is_warning_red_80dp
            ))
        }
    }

    override fun showFinishProcessingBox() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "115",
                    message = context.getString(R.string.finish_processing_box),
                    iconRes = R.drawable.is_warning_red_80dp
            ))
        }
    }

    override fun showFinishProcessingCurrentBox() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "117",
                    message = context.getString(R.string.finish_processing_current_box),
                    iconRes = R.drawable.is_warning_red_80dp
            ))
        }
    }

}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openFastDataLoadingScreen()
    fun openSelectMarketScreen()
    fun openEnterEmployeeNumberScreen()
    fun openMainMenuScreen()

    fun openTaskCompositionScreen()
    fun openBasketGoodListScreen()
    fun openGoodDetailsScreen()
    fun openSaveDataScreen()
    fun openTaskListScreen()
    fun openBasketPropertiesScreen()
    fun openDiscrepancyListScreen()
    fun openGoodInfoScreen()
    fun openGoodListScreen()
    fun openTaskCardScreen()
    fun openTaskSearchScreen()

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
    fun showProductDoesNotMatchTaskSettings()
    fun openScannedMarkIsNotOnBalanceInCurrentStore(proceedCallback: () -> Unit)
    fun showScannedBoxIsNotWhole()
    fun showMarksInBoxAreNotOnBalanceInCurrentStore()
    fun showFinishProcessingBox()
    fun showFinishProcessingCurrentBox()
    fun openAddSupplierScreen()
}