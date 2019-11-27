package com.lenta.bp16.platform.navigation

import android.content.Context
import com.lenta.bp16.R
import com.lenta.bp16.features.auth.AuthFragment
import com.lenta.bp16.features.good_packaging.GoodPackagingFragment
import com.lenta.bp16.features.good_weighing.GoodWeighingFragment
import com.lenta.bp16.features.raw_good_list.RawGoodListFragment
import com.lenta.bp16.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp16.features.main_menu.MainMenuFragment
import com.lenta.bp16.features.pack_good_list.PackGoodListFragment
import com.lenta.bp16.features.pack_list.PackListFragment
import com.lenta.bp16.features.raw_list.RawListFragment
import com.lenta.bp16.features.select_market.SelectMarketFragment
import com.lenta.bp16.features.task_list.TaskListFragment
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

    override fun openTaskListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TaskListFragment())
        }
    }

    override fun openRawGoodListScreen() {
        runOrPostpone {
            getFragmentStack()?.push(RawGoodListFragment())
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

    override fun showConfirmNoSuchItemLeft(confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    pageNumber = "23",
                    message = context.getString(R.string.confirm_that_there_is_no_such_item_left_in_pu),
                    iconRes = R.drawable.is_warning_yellow_80dp,
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

}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openFastDataLoadingScreen()
    fun openSelectMarketScreen()
    fun openMainMenuScreen()
    fun openTaskListScreen()
    fun openRawGoodListScreen()
    fun openRawListScreen()
    fun openGoodWeighingScreen()
    fun openGoodPackagingScreen()
    fun openPackListScreen()
    fun openPackGoodListScreen()

    fun showDefrostingPhaseIsCompleted(nextCallback: () -> Unit)
    fun showConfirmNoSuchItemLeft(confirmCallback: () -> Unit)
    fun showFixingPackagingPhaseSuccessful(nextCallback: () -> Unit)
}