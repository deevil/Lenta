package com.lenta.bp9.platform.navigation

import android.content.Context
import com.lenta.bp9.features.auth.AuthFragment
import com.lenta.bp9.features.loading.tasks.LoadingTasksFragment
import com.lenta.bp9.features.task_list.TaskListFragment
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.requests.TaskListSearchParams
import com.lenta.bp9.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp9.features.main_menu.MainMenuFragment
import com.lenta.bp9.features.select_market.SelectMarketFragment
import com.lenta.bp9.features.select_personnel_number.SelectPersonnelNumberFragment
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
            getFragmentStack()?.replace(SelectMarketFragment())
        }
    }

    override fun openMainMenuScreen() {
        runOrPostpone {
            getFragmentStack()?.replace(MainMenuFragment())
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

    override fun openTaskListLoadingScreen(mode: TaskListLoadingMode, searchParams: TaskListSearchParams?) {
        runOrPostpone {
            getFragmentStack()?.push(LoadingTasksFragment.create(searchParams, mode))
        }
    }

    override fun openFastDataLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FastDataLoadingFragment())
        }
    }

    override fun openSelectionPersonnelNumberScreen() {
        runOrPostpone {
            getFragmentStack()?.replace(SelectPersonnelNumberFragment())
        }
    }

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openSelectMarketScreen()
    fun openMainMenuScreen()
    fun openLoginScreen()
    fun openTaskListScreen()
    fun openTaskListLoadingScreen(mode: TaskListLoadingMode, searchParams: TaskListSearchParams? = null)
    fun openFastDataLoadingScreen()
    fun openSelectionPersonnelNumberScreen()
}