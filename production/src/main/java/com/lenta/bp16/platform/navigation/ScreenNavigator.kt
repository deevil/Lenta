package com.lenta.bp16.platform.navigation

import android.content.Context
import com.lenta.bp16.R
import com.lenta.bp16.features.auth.AuthFragment
import com.lenta.bp16.features.external_supply_list.ExternalSupplyListFragment
import com.lenta.bp16.features.external_supply_task_list.ExternalSupplyTaskListFragment
import com.lenta.bp16.features.good_packaging.GoodPackagingFragment
import com.lenta.bp16.features.good_weighing.GoodWeighingFragment
import com.lenta.bp16.features.processing_unit_list.ProcessingUnitListFragment
import com.lenta.bp16.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp16.features.main_menu.MainMenuFragment
import com.lenta.bp16.features.pack_good_list.PackGoodListFragment
import com.lenta.bp16.features.pack_list.PackListFragment
import com.lenta.bp16.features.raw_list.RawListFragment
import com.lenta.bp16.features.select_market.SelectMarketFragment
import com.lenta.bp16.features.processing_unit_task_list.ProcessingUnitTaskListFragment
import com.lenta.bp16.features.reprint_label.ReprintLabelFragment
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

    override fun openProcessingUnitTaskListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ProcessingUnitTaskListFragment())
        }
    }

    override fun openExternalSupplyTaskListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ExternalSupplyTaskListFragment())
        }
    }

    override fun openProcessingUnitListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ProcessingUnitListFragment())
        }
    }

    override fun openExternalSupplyListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ExternalSupplyListFragment())
        }
    }

    override fun openRawListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(RawListFragment())
        }
    }

    override fun openGoodWeighingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodWeighingFragment())
        }
    }

    override fun openGoodPackagingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodPackagingFragment())
        }
    }

    override fun openPackListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(PackListFragment())
        }
    }

    override fun openPackGoodListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(PackGoodListFragment())
        }
    }

    override fun openReprintLabelScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ReprintLabelFragment())
        }
    }


    // Информационные экраны
    override fun showDefrostingPhaseIsCompleted(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "12",
                    message = context.getString(R.string.defrosting_phase_is_completed),
                    iconRes = R.drawable.is_warning_yellow_80dp,
                    isVisibleLeftButton = false,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next
            ))
        }
    }

    override fun showConfirmNoSuchItemLeft(taskType: String, confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "23",
                    message = context.getString(R.string.confirm_that_there_is_no_such_item_left, taskType),
                    iconRes = R.drawable.is_warning_red_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm
            ))
        }
    }

    override fun showConfirmNoRawItem(taskType: String, confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "24",
                    message = context.getString(R.string.confirm_no_raw_items, taskType),
                    iconRes = R.drawable.is_warning_red_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm
            ))
        }
    }

    override fun showFixingPackagingPhaseSuccessful(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "35",
                    message = context.getString(R.string.fixing_beginning_of_packaging_phase_was_successful),
                    iconRes = R.drawable.is_warning_yellow_80dp,
                    isVisibleLeftButton = false,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.next
            ))
        }
    }

    override fun showMoreThanOneOrderForThisProduct() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "32.1",
                    message = context.getString(R.string.more_than_one_order_for_this_product),
                    iconRes = R.drawable.is_warning_red_80dp
            ))
        }
    }

    /*override fun showErrorCompletingObjectProcessing() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "31.5",
                    message = context.getString(R.string.error_completing_object_processing),
                    iconRes = R.drawable.is_warning_red_80dp
            ))
        }
    }*/

    override fun showNotSavedDataWillBeLost(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "36",
                    message = context.getString(R.string.not_saved_data_will_be_lost),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes
            ))
        }
    }

    override fun showAlertNoIpPrinter() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "36",
                    message = context.getString(R.string.no_ip_printer_alert)
            ))
        }
    }

    override fun showLabelSentToPrint(nextCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "99",
                    message = context.getString(R.string.label_sent_to_print),
                    iconRes = R.drawable.ic_question_80dp,
                    isVisibleLeftButton = false,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(nextCallback),
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
    fun openMainMenuScreen()
    fun openProcessingUnitTaskListScreen()
    fun openExternalSupplyTaskListScreen()
    fun openProcessingUnitListScreen()
    fun openExternalSupplyListScreen()
    fun openRawListScreen()
    fun openGoodWeighingScreen()
    fun openGoodPackagingScreen()
    fun openPackListScreen()
    fun openPackGoodListScreen()
    fun openReprintLabelScreen()

    fun showDefrostingPhaseIsCompleted(nextCallback: () -> Unit)
    fun showConfirmNoSuchItemLeft(taskType: String, confirmCallback: () -> Unit)
    fun showConfirmNoRawItem(taskType: String, confirmCallback: () -> Unit)
    fun showFixingPackagingPhaseSuccessful(nextCallback: () -> Unit)
    fun showMoreThanOneOrderForThisProduct()
    fun showNotSavedDataWillBeLost(yesCallback: () -> Unit)
    fun showAlertNoIpPrinter()
    fun showLabelSentToPrint(nextCallback: () -> Unit)
    //fun showErrorCompletingObjectProcessing()
}