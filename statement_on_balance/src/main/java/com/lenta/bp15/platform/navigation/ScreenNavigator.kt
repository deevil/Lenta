package com.lenta.bp15.platform.navigation

import android.content.Context
import com.lenta.bp15.features.auth.AuthFragment
import com.lenta.bp15.features.discrepancy_list.DiscrepancyListFragment
import com.lenta.bp15.features.enter_employee_number.EnterEmployeeNumberFragment
import com.lenta.bp15.features.good_list.GoodListFragment
import com.lenta.bp15.features.loading.FastDataLoadingFragment
import com.lenta.bp15.features.main_menu.MainMenuFragment
import com.lenta.bp15.features.select_market.SelectMarketFragment
import com.lenta.bp15.features.task_list.TaskListFragment
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
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


    // Информационные экраны


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

}