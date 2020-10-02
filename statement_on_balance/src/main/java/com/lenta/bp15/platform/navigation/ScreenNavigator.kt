package com.lenta.bp15.platform.navigation

import android.content.Context
import com.lenta.bp15.R
import com.lenta.bp15.features.auth.AuthFragment
import com.lenta.bp15.features.discrepancy_list.DiscrepancyListFragment
import com.lenta.bp15.features.enter_employee_number.EnterEmployeeNumberFragment
import com.lenta.bp15.features.good_info.GoodInfoFragment
import com.lenta.bp15.features.good_list.GoodListFragment
import com.lenta.bp15.features.loading.FastDataLoadingFragment
import com.lenta.bp15.features.main_menu.MainMenuFragment
import com.lenta.bp15.features.search_task.SearchTaskFragment
import com.lenta.bp15.features.select_market.SelectMarketFragment
import com.lenta.bp15.features.task_card.TaskCardFragment
import com.lenta.bp15.features.task_list.TaskListFragment
import com.lenta.shared.account.IAuthenticator
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
    override fun openTaskListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskListFragment())
        }
    }

    override fun openGoodListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodListFragment())
        }
    }

    override fun openDiscrepancyListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(DiscrepancyListFragment())
        }
    }

    override fun openTaskCardScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskCardFragment())
        }
    }

    override fun openGoodInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoFragment())
        }
    }

    override fun openSearchTaskScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SearchTaskFragment())
        }
    }


    // Информационные экраны
    override fun showMarkedGoodInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.marked_good),
                    iconRes = R.drawable.ic_marked_white_32dp), CustomAnimation.vertical
            )
        }
    }

    override fun showUnsavedDataFoundOnDevice(deleteCallback: () -> Unit, goOverCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.unsaved_data_found_on_device),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    codeConfirmForButton3 = backFragmentResultHelper.setFuncForResult(deleteCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(goOverCallback),
                    buttonDecorationInfo3 = ButtonDecorationInfo.delete,
                    rightButtonDecorationInfo = ButtonDecorationInfo.goOver
            ))
        }
    }

    override fun showMakeTaskProcessedAndClose(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.make_task_processed_and_close),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes
            ))
        }
    }

    override fun showSuccessSaveData() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.success_save_report),
                    iconRes = R.drawable.ic_done_green_80dp,
                    timeAutoExitInMillis = 3000,
                    isVisibleLeftButton = false
            ))
        }
    }

    override fun showUnprocessedGoodsInTask(publishedCallback: () -> Unit, processedCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.unprocessed_goods_in_task),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    codeConfirmForButton3 = backFragmentResultHelper.setFuncForResult(publishedCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(processedCallback),
                    buttonDecorationInfo3 = ButtonDecorationInfo.published,
                    rightButtonDecorationInfo = ButtonDecorationInfo.processed
            ))
        }
    }

    override fun showRequiredToDestroyNonGluedStamps(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.required_to_destroy_non_glued_stamps),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next
            ))
        }
    }

    override fun showGoodIsMissingFromTask(material: String) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.good_is_missing_from_task, material),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showStampAlreadyProcessed() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.stamp_already_processed),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showInvalidBarcodeFormatScanned() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.invalid_barcode_format_scanned),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showScannedStampIsNotOnTask() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.scanned_stamp_is_not_on_task),
                    iconRes = R.drawable.ic_warning_red_80dp
            ))
        }
    }

    override fun showTaskUnsentDataWillBeDeleted(taskName: String, applyCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.task_unsent_data_will_be_deleted, taskName),
                    iconRes = R.drawable.ic_delete_red_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(applyCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.apply
            ))
        }
    }

    override fun showUnsavedDataWillBeRemoved(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.unsaved_data_will_be_removed),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.back,
                    rightButtonDecorationInfo = ButtonDecorationInfo.next
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

    fun openTaskListScreen()
    fun openGoodListScreen()
    fun openDiscrepancyListScreen()
    fun openTaskCardScreen()
    fun openGoodInfoScreen()
    fun openSearchTaskScreen()

    fun showMarkedGoodInfoScreen()
    fun showUnsavedDataFoundOnDevice(deleteCallback: () -> Unit, goOverCallback: () -> Unit)
    fun showMakeTaskProcessedAndClose(yesCallback: () -> Unit)
    fun showSuccessSaveData()
    fun showUnprocessedGoodsInTask(publishedCallback: () -> Unit, processedCallback: () -> Unit)
    fun showRequiredToDestroyNonGluedStamps(nextCallback: () -> Unit)
    fun showGoodIsMissingFromTask(material: String)
    fun showStampAlreadyProcessed()
    fun showInvalidBarcodeFormatScanned()
    fun showScannedStampIsNotOnTask()
    fun showTaskUnsentDataWillBeDeleted(taskName: String, applyCallback: () -> Unit)
    fun showUnsavedDataWillBeRemoved(nextCallback: () -> Unit)

}