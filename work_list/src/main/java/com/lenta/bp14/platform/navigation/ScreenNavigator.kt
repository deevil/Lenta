package com.lenta.bp14.platform.navigation

import android.content.Context
import com.lenta.bp14.features.auth.AuthFragment
import com.lenta.bp14.features.check_list.goods_list.GoodsListClFragment
import com.lenta.bp14.features.job_card.JobCardFragment
import com.lenta.bp14.features.list_of_differences.ListOfDifferencesFragment
import com.lenta.bp14.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp14.features.main_menu.MainMenuFragment
import com.lenta.bp14.features.select_market.SelectMarketFragment
import com.lenta.bp14.features.task_list.TaskListFragment
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
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

    override fun openCheckListGoodsList() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsListClFragment())
        }
    }

    override fun openListOfDifferencesScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ListOfDifferencesFragment())
        }
    }


    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openSelectMarketScreen()
    fun openMainMenuScreen()
    fun openLoginScreen()
    fun openFastDataLoadingScreen()
    fun openTaskListScreen()
    fun openJobCardScreen(taskNumber: String)
    fun openCheckListGoodsList()
    fun openListOfDifferencesScreen()
}