package com.lenta.bp10.platform.navigation

import com.lenta.bp10.features.alert.AlertFragment
import com.lenta.bp10.features.auth.AuthFragment
import com.lenta.bp10.features.auxiliary_menu.AuxiliaryMenuFragment
import com.lenta.bp10.features.job_card.JobCardFragment
import com.lenta.bp10.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp10.features.loading.tasks_settings.LoadingTaskSettingsFragment
import com.lenta.bp10.features.main_menu.MainMenuFragment
import com.lenta.bp10.features.select_market.SelectMarketFragment
import com.lenta.bp10.features.select_oper_mode.SelectOperModeFragment
import com.lenta.bp10.features.select_personnel_number.SelectPersonnelNumberFragment
import com.lenta.bp10.features.settings.SettingsFragment
import com.lenta.bp10.features.support.SupportFragment
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.CustomAnimation
import com.lenta.shared.platform.navigation.IGoBackNavigator
import com.lenta.shared.progress.IProgressUseCaseInformator
import com.lenta.shared.utilities.Logg

class ScreenNavigator(
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator,
        private val failureInterpreter: IFailureInterpreter,
        private val progressUseCaseInformator: IProgressUseCaseInformator
) : IScreenNavigator {

    override fun openAlertScreen(message: String) {
        getFragmentStack()?.let {
            val fragment = AlertFragment.create(message)
            it.push(fragment, CustomAnimation.vertical())

        }
    }

    override fun openAlertScreen(failure: Failure) {
        openAlertScreen(failureInterpreter.getFailureDescription(failure))
    }

    override fun goBack() {
        getFragmentStack()?.pop()
    }

    override fun openSelectMarketScreen() {
        getFragmentStack()?.replace(SelectMarketFragment())
    }

    override fun openFirstScreen() {
        if (authenticator.isAuthorized()) {

        } else {
            openLoginScreen()
        }
    }

    override fun openLoginScreen() {
        Logg.d()
        getFragmentStack()?.let {
            it.popAll()
            it.replace(AuthFragment())
        }
    }

    override fun openFastDataLoadingScreen() {
        getFragmentStack()?.push(FastDataLoadingFragment())
    }

    override fun openSelectionTabNumberScreen() {
        getFragmentStack()?.replace(SelectPersonnelNumberFragment())
    }

    override fun openAuxiliaryMenuScreen() {
        getFragmentStack()?.push(AuxiliaryMenuFragment())
    }


    override fun openSelectOperModeScreen() {
        getFragmentStack()?.push(SelectOperModeFragment())
    }


    override fun hideProgress() {
        foregroundActivityProvider.getActivity()?.getViewModel()?.hideProgress()
    }

    override fun <Params> showProgress(useCase: UseCase<Any, Params>) {
        showProgress(progressUseCaseInformator.getTitle(useCase))
    }

    private fun showProgress(title: String) {
        foregroundActivityProvider.getActivity()?.getViewModel()?.showSimpleProgress(title)
    }

    override fun openMainMenuScreen() {
        getFragmentStack()?.push(MainMenuFragment())
    }

    override fun openJobCardScreen() {
        getFragmentStack()?.push(JobCardFragment())
    }

    override fun openLoadingTaskSettingsScreen() {
        getFragmentStack()?.push(LoadingTaskSettingsFragment())
    }

    override fun openSettingsScreen() {
        getFragmentStack()?.push(SettingsFragment())
    }

    override fun openSupportScreen() {
        getFragmentStack()?.push(SupportFragment())
    }

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

interface IScreenNavigator : IGoBackNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openSelectMarketScreen()
    fun openAlertScreen(message: String)
    fun openAlertScreen(failure: Failure)
    fun openFastDataLoadingScreen()
    fun openSelectionTabNumberScreen()
    fun openAuxiliaryMenuScreen()
    fun openSelectOperModeScreen()
    fun openSettingsScreen()
    fun openSupportScreen()
    fun hideProgress()
    fun <Params> showProgress(useCase: UseCase<Any, Params>)
    fun openMainMenuScreen()
    fun openJobCardScreen()
    fun openLoadingTaskSettingsScreen()
}