package com.lenta.bp12.platform.navigation

import android.content.Context
import com.lenta.bp12.R
import com.lenta.bp12.features.auth.AuthFragment
import com.lenta.bp12.features.basket.basket_good_list.BasketCreateGoodListFragment
import com.lenta.bp12.features.basket.basket_good_list.BasketOpenGoodListFragment
import com.lenta.bp12.features.basket.basket_properties.BasketPropertiesFragment
import com.lenta.bp12.features.create_task.add_provider.AddProviderFragment
import com.lenta.bp12.features.create_task.good_details.GoodDetailsCreateFragment
import com.lenta.bp12.features.create_task.good_info.GoodInfoCreateFragment
import com.lenta.bp12.features.create_task.marked_good_info.MarkedGoodInfoCreateFragment
import com.lenta.bp12.features.create_task.task_card.TaskCardCreateFragment
import com.lenta.bp12.features.create_task.task_content.TaskContentFragment
import com.lenta.bp12.features.enter_employee_number.EnterEmployeeNumberFragment
import com.lenta.bp12.features.enter_mrc.EnterMrcFragment
import com.lenta.bp12.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp12.features.main_menu.MainMenuFragment
import com.lenta.bp12.features.open_task.discrepancy_list.DiscrepancyListFragment
import com.lenta.bp12.features.open_task.good_details.GoodDetailsOpenFragment
import com.lenta.bp12.features.open_task.good_info.GoodInfoOpenFragment
import com.lenta.bp12.features.open_task.good_list.GoodListFragment
import com.lenta.bp12.features.open_task.marked_good_info.MarkedGoodInfoOpenFragment
import com.lenta.bp12.features.open_task.task_card.TaskCardOpenFragment
import com.lenta.bp12.features.open_task.task_list.TaskListFragment
import com.lenta.bp12.features.open_task.task_search.TaskSearchFragment
import com.lenta.bp12.features.save_data.SaveDataFragment
import com.lenta.bp12.features.select_market.SelectMarketFragment
import com.lenta.bp12.model.WorkType
import com.lenta.bp12.model.pojo.Good
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.CustomAnimation
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
            getFragmentStack()?.push(TaskContentFragment())
        }
    }

    override fun openBasketCreateGoodListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(BasketCreateGoodListFragment())
        }
    }

    override fun openBasketOpenGoodListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(BasketOpenGoodListFragment())
        }
    }

    override fun openGoodDetailsCreateScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodDetailsCreateFragment())
        }
    }

    override fun openGoodDetailsOpenScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodDetailsOpenFragment())
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

    override fun openGoodInfoCreateScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoCreateFragment())
        }
    }

    override fun openGoodInfoOpenScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoOpenFragment())
        }
    }

    override fun openGoodListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodListFragment())
        }
    }

    override fun openTaskCardCreateScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskCardCreateFragment())
        }
    }

    override fun openTaskCardOpenScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskCardOpenFragment())
        }
    }

    override fun openTaskSearchScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskSearchFragment())
        }
    }

    override fun openAddProviderScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AddProviderFragment())
        }
    }

    override fun openMarkedGoodInfoCreateScreen() {
        runOrPostpone {
            getFragmentStack()?.push(MarkedGoodInfoCreateFragment())
        }
    }

    override fun openMarkedGoodInfoOpenScreen() {
        runOrPostpone {
            getFragmentStack()?.push(MarkedGoodInfoOpenFragment())
        }
    }

    override fun openEnterMrcFromBoxScreen(workType: WorkType, onNextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(
                    EnterMrcFragment.newInstance(
                            workType,
                            backFragmentResultHelper.setFuncForResult(onNextCallback)
                    )
            )
        }
    }

    // Информационные экраны
    override fun showUnsentDataFoundOnDevice(deleteCallback: () -> Unit, goOverCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "5",
                    message = context.getString(R.string.unsent_data_found_on_device),
                    iconRes = R.drawable.ic_question_yellow_80dp,
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
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(proceedCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.proceed,
                    leftButtonDecorationInfo = ButtonDecorationInfo.cancel
            ))
        }
    }

    override fun showMakeTaskCountedAndClose(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "19",
                    message = context.getString(R.string.make_task_counted_and_close),
                    iconRes = R.drawable.ic_question_yellow_80dp,
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
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showForExciseGoodNeedScanFirstMark() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "93",
                    message = context.getString(R.string.for_excise_alcohol_need_scan_first_mark),
                    iconRes = R.drawable.ic_info_green_80dp,
                    timeAutoExitInMillis = 3000
            ))
        }
    }

    override fun showForGoodNeedScanFirstMark(goodTitle: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    title = goodTitle,
                    pageNumber = "85",
                    message = context.getString(R.string.for_good_need_scan_first_mark),
                    iconRes = R.drawable.ic_info_green_80dp,
                    timeAutoExitInMillis = 3000
            ))
        }
    }

    override fun showRawGoodsRemainedInTask(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "36",
                    message = context.getString(R.string.raw_goods_remained_in_task),
                    iconRes = R.drawable.ic_question_yellow_80dp,
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
                    iconRes = R.drawable.ic_warning_red_80dp,
                    codeConfirmForExit = backFragmentResultHelper.setFuncForResult(afterShowCallback),
                    timeAutoExitInMillis = 2000
            ))
        }
    }

    override fun showDoYouReallyWantSetZeroQuantity(count: Int, yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "65",
                    message = context.getString(R.string.do_you_really_want_set_zero_quantity, count),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes
            ))
        }
    }

    override fun showNotMatchTaskSettingsAddingNotPossible() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "73",
                    message = context.getString(R.string.not_match_task_settings_adding_not_possible),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showGoodCannotBeAdded() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "73.1",
                    message = context.getString(R.string.good_cannot_be_added),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun openScannedMarkIsNotOnBalanceInCurrentStore(proceedCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "84",
                    message = context.getString(R.string.scanned_mark_is_not_on_balance_in_current_store),
                    iconRes = R.drawable.ic_warning_red_80dp,
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
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showMarksInBoxAreNotOnBalanceInCurrentStore() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "91",
                    message = context.getString(R.string.marks_in_box_are_not_on_balance_in_current_store),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showFinishProcessingBox() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "115",
                    message = context.getString(R.string.finish_processing_box),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showFinishProcessingCurrentBox() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "117",
                    message = context.getString(R.string.finish_processing_current_box),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showGoodIsMissingInTask() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "118",
                    message = context.getString(R.string.good_is_missing_in_task)
            ))
        }
    }

    override fun showCantScanPackAlert() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "100",
                    message = context.getString(R.string.cant_scan_tobacco_pack_scan_carton),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    // Описание иконок
    override fun showExciseAlcoholGoodInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.alcohol_good),
                    iconRes = com.lenta.shared.R.drawable.ic_excise_alcohol_white_32dp), CustomAnimation.vertical)
        }
    }

    override fun showAlcoholGoodInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.alcohol_good),
                    iconRes = com.lenta.shared.R.drawable.ic_alcohol_white_32dp), CustomAnimation.vertical)
        }
    }

    override fun showCommonGoodInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.common_good),
                    iconRes = com.lenta.shared.R.drawable.ic_kandy_white_32dp), CustomAnimation.vertical)
        }
    }

    override fun showMarkedGoodInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.marked_good),
                    iconRes = com.lenta.shared.R.drawable.ic_marked_white_32dp), CustomAnimation.vertical)
        }
    }

    override fun showCloseBasketDialog(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "71",
                    message = context.getString(R.string.close_basket),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes
            ))
        }
    }

    override fun showOpenBasketDialog(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "71",
                    message = context.getString(R.string.open_basket),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes
            ))
        }
    }

    override fun showSomeOfChosenBasketsNotClosedScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "79",
                    message = context.getString(R.string.some_of_chosen_baskets_not_closed),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showSomeBasketsNotClosedCantSaveScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "84",
                    message = context.getString(R.string.some_baskets_not_closed_cant_save),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showSomeBasketsAlreadyPrinted(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "81",
                    message = context.getString(R.string.some_baskets_already_printed),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes
            ))
        }
    }

    override fun showPalletListPrintedScreen(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "81",
                    message = context.getString(R.string.pallet_list_printed),
                    iconRes = R.drawable.ic_info_green_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next,
                    leftButtonDecorationInfo = ButtonDecorationInfo.empty,
                    isVisibleLeftButton = false
            ))
        }
    }

    override fun showQuantityMoreThanPlannedScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "87",
                    message = context.getString(R.string.quantity_more_than_planned),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showMarkAlreadyScannedDelete(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "93",
                    message = context.getString(R.string.mark_already_scanned_delete),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback)
            ))
        }
    }

    override fun showCartonAlreadyScannedDelete(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "91",
                    message = context.getString(R.string.carton_already_scanned_delete),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback)
            ))
        }
    }

    override fun showBoxAlreadyScannedDelete(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "89",
                    message = context.getString(R.string.box_already_scanned_delete),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback)
            ))
        }
    }

    override fun showMrcNotSameAlert(good: Good) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "98",
                    message = context.getString(R.string.scanned_wrong_mrc, good.ean, good.name),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showMrcNotSameInBasketAlert(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "89",
                    message = context.getString(R.string.scanned_mark_with_different_mrc),
                    iconRes = R.drawable.ic_info_green_80dp,
                    rightButtonDecorationInfo = ButtonDecorationInfo.next,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback)
            ))
        }
    }

    override fun showNoMarkTypeInSettings() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "98",
                    message = context.getString(R.string.no_settings_for_that_markType),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showCantAddExciseGoodForWholesale() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "98",
                    message = context.getString(R.string.cant_scan_excise_for_wholesale_task),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showCantAddVetToWholeSale() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "98",
                    message = context.getString(R.string.cant_scan_vet_for_wholesale_task),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showMarkScanError(errorText: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "98",
                    message = errorText,
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showAlertDialogWithRedTriangle(errorText: String, screenNumber: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = screenNumber,
                    message = errorText,
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showChooseProviderFirst() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "98",
                    message = context.getString(R.string.choose_provider),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showInternalError(cause: String) {
        openAlertScreen(Failure.MessageFailure("Внутренняя ошибка программы: $cause"))
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
    fun openBasketCreateGoodListScreen()
    fun openBasketOpenGoodListScreen()
    fun openGoodDetailsCreateScreen()
    fun openGoodDetailsOpenScreen()
    fun openSaveDataScreen()
    fun openTaskListScreen()
    fun openBasketPropertiesScreen()
    fun openDiscrepancyListScreen()

    fun openGoodListScreen()
    fun openTaskCardCreateScreen()
    fun openTaskCardOpenScreen()
    fun openTaskSearchScreen()
    fun openAddProviderScreen()

    fun openGoodInfoCreateScreen()
    fun openGoodInfoOpenScreen()
    fun openMarkedGoodInfoCreateScreen()
    fun openMarkedGoodInfoOpenScreen()
    fun openEnterMrcFromBoxScreen(workType: WorkType, nextCallback: () -> Unit)

    fun showUnsentDataFoundOnDevice(deleteCallback: () -> Unit, goOverCallback: () -> Unit)
    fun showUnsavedDataWillBeLost(proceedCallback: () -> Unit)
    fun showMakeTaskCountedAndClose(yesCallback: () -> Unit)
    fun showTaskUnsentDataWillBeDeleted(taskName: String, applyCallback: () -> Unit)
    fun showScannedMarkBelongsToProduct(productName: String)
    fun showForExciseGoodNeedScanFirstMark()

    fun showForGoodNeedScanFirstMark(goodTitle: String)
    fun showRawGoodsRemainedInTask(yesCallback: () -> Unit)
    fun showBoxWasLastScanned(afterShowCallback: () -> Unit)
    fun showDoYouReallyWantSetZeroQuantity(count: Int, yesCallback: () -> Unit)
    fun showNotMatchTaskSettingsAddingNotPossible()
    fun showGoodCannotBeAdded()
    fun openScannedMarkIsNotOnBalanceInCurrentStore(proceedCallback: () -> Unit)
    fun showScannedBoxIsNotWhole()
    fun showMarksInBoxAreNotOnBalanceInCurrentStore()
    fun showFinishProcessingBox()
    fun showFinishProcessingCurrentBox()
    fun showGoodIsMissingInTask()
    fun showCantScanPackAlert()

    fun showExciseAlcoholGoodInfoScreen()
    fun showAlcoholGoodInfoScreen()
    fun showCommonGoodInfoScreen()
    fun showMarkedGoodInfoScreen()

    fun showCloseBasketDialog(yesCallback: () -> Unit)
    fun showOpenBasketDialog(yesCallback: () -> Unit)

    fun showSomeOfChosenBasketsNotClosedScreen()
    fun showSomeBasketsNotClosedCantSaveScreen()
    fun showSomeBasketsAlreadyPrinted(yesCallback: () -> Unit)
    fun showPalletListPrintedScreen(nextCallback: () -> Unit)

    fun showInternalError(cause: String)
    fun showQuantityMoreThanPlannedScreen()

    fun showMarkAlreadyScannedDelete(yesCallback: () -> Unit)
    fun showCartonAlreadyScannedDelete(yesCallback: () -> Unit)
    fun showBoxAlreadyScannedDelete(yesCallback: () -> Unit)
    fun showMrcNotSameAlert(good: Good)
    fun showMrcNotSameInBasketAlert(yesCallback: () -> Unit)

    fun showNoMarkTypeInSettings()

    fun showChooseProviderFirst()

    fun showCantAddExciseGoodForWholesale()
    fun showCantAddVetToWholeSale()

    fun showMarkScanError(errorText: String)

    fun showAlertDialogWithRedTriangle(errorText: String, screenNumber: String = "97")
}