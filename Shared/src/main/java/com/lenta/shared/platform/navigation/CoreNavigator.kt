package com.lenta.shared.platform.navigation

import android.content.Context
import android.os.Bundle
import com.lenta.shared.R
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.features.auxiliary_menu.AuxiliaryMenuFragment
import com.lenta.shared.features.fmp_settings.FmpSettingsFragment
import com.lenta.shared.features.printer_change.PrinterChangeFragment
import com.lenta.shared.features.select_oper_mode.SelectOperModeFragment
import com.lenta.shared.features.settings.SettingsFragment
import com.lenta.shared.features.support.SupportFragment
import com.lenta.shared.features.tech_login.TechLoginFragment
import com.lenta.shared.features.test_environment.TestEnvirFragment
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.platform.activity.ForegroundActivityProvider


class CoreNavigator constructor(private val context: Context,
                                private val foregroundActivityProvider: ForegroundActivityProvider,
                                private val failureInterpreter: IFailureInterpreter) : ICoreNavigator {
    override fun goBackWithArgs(args: Bundle) {
        getFragmentStack()?.popReturnArgs(args = args)
    }

    override fun goBack() {
        getFragmentStack()?.pop()
    }

    override fun finishApp() {
        foregroundActivityProvider.getActivity()?.finish()
        System.exit(0)
    }

    override fun openAlertScreen(message: String) {
        getFragmentStack()?.let {
            val fragment = AlertFragment.create(message)
            it.push(fragment, CustomAnimation.vertical())

        }
    }

    override fun openAlertScreen(failure: Failure) {
        openAlertScreen(failureInterpreter.getFailureDescription(failure))
    }

    override fun openSupportScreen() {
        getFragmentStack()?.push(SupportFragment())
    }

    override fun <Params> showProgress(useCase: UseCase<Any, Params>) {
        showProgress(context.getString(R.string.data_loading))
    }

    override fun showProgress(title: String) {
        foregroundActivityProvider.getActivity()?.getViewModel()?.showSimpleProgress(title)
    }

    override fun hideProgress() {
        foregroundActivityProvider.getActivity()?.getViewModel()?.hideProgress()
    }

    override fun openTechLoginScreen() {
        getFragmentStack()?.push(TechLoginFragment())
    }

    override fun openConnectionsSettingsScreen() {
        getFragmentStack()?.push(FmpSettingsFragment())
    }

    override fun openTestEnvirScreen() {
        getFragmentStack()?.push(TestEnvirFragment())
    }

    override fun openSelectOperModeScreen() {
        getFragmentStack()?.push(SelectOperModeFragment())
    }

    override fun openPrinterChangeScreen() {
        getFragmentStack()?.push(PrinterChangeFragment())
    }

    override fun openSettingsScreen() {
        getFragmentStack()?.push(SettingsFragment())
    }

    override fun openAuxiliaryMenuScreen() {
        getFragmentStack()?.push(AuxiliaryMenuFragment())
    }

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

interface ICoreNavigator {
    fun goBackWithArgs(args: Bundle)
    fun goBack()
    fun finishApp()
    fun openAlertScreen(message: String)
    fun openAlertScreen(failure: Failure)
    fun openSupportScreen()
    fun <Params> showProgress(useCase: UseCase<Any, Params>)
    fun showProgress(title: String)
    fun hideProgress()
    fun openTechLoginScreen()
    fun openConnectionsSettingsScreen()
    fun openTestEnvirScreen()
    fun openSelectOperModeScreen()
    fun openPrinterChangeScreen()
    fun openSettingsScreen()
    fun openAuxiliaryMenuScreen()
}