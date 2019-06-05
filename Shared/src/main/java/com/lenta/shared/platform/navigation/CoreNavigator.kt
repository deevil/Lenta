package com.lenta.shared.platform.navigation

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import com.lenta.shared.R
import com.lenta.shared.analytics.IAnalytics
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.features.auxiliary_menu.AuxiliaryMenuFragment
import com.lenta.shared.features.exit.ExitWithConfirmationFragment
import com.lenta.shared.features.fmp_settings.FmpSettingsFragment
import com.lenta.shared.features.printer_change.PrinterChangeFragment
import com.lenta.shared.features.select_oper_mode.SelectOperModeFragment
import com.lenta.shared.features.select_oper_mode.SelectOperModeViewModel
import com.lenta.shared.features.settings.SettingsFragment
import com.lenta.shared.features.support.SupportFragment
import com.lenta.shared.features.tech_login.TechLoginFragment
import com.lenta.shared.features.test_environment.PinCodeFragment
import com.lenta.shared.features.test_environment.failure.FailurePinCodeFragment
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.utilities.extentions.setFragmentResultCode


class CoreNavigator constructor(private val context: Context,
                                private val foregroundActivityProvider: ForegroundActivityProvider,
                                private val failureInterpreter: IFailureInterpreter,
                                private val analytics: IAnalytics) : ICoreNavigator {

    override val functionsCollector: FunctionsCollector by lazy {
        FunctionsCollector(foregroundActivityProvider.onPauseStateLiveData)
    }


    override fun goBackWithArgs(args: Bundle) {
        runOrPostpone {
            getFragmentStack()?.popReturnArgs(args = args)
        }
    }

    override fun goBackWithResultCode(code: Int) {
        goBackWithArgs(
                args = Bundle().apply {
                    setFragmentResultCode(code)
                })
    }

    override fun goBack() {
        runOrPostpone {
            getFragmentStack()?.pop()
        }
    }

    override fun finishApp() {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.finish()
            analytics.cleanLogs()
            System.exit(0)
        }

    }


    override fun openAlertScreen(message: String, pageNumber: String) {
        runOrPostpone {
            getFragmentStack()?.let {
                val fragment = AlertFragment.create(message = message, pageNumber = pageNumber)
                it.push(fragment, CustomAnimation.vertical())

            }
        }
    }

    override fun openAlertScreen(failure: Failure, pageNumber: String) {
        runOrPostpone {
            openAlertScreen(message = failureInterpreter.getFailureDescription(failure), pageNumber = pageNumber)
        }
    }

    override fun openSupportScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SupportFragment())
        }
    }

    override fun <Params> showProgress(useCase: UseCase<Any, Params>) {
        runOrPostpone {
            showProgress(context.getString(R.string.data_loading))
        }
    }

    override fun showProgress(title: String) {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.getViewModel()?.showSimpleProgress(title)
        }
    }

    override fun hideProgress() {
        runOrPostpone {
            foregroundActivityProvider.getActivity()?.getViewModel()?.hideProgress()
        }
    }

    override fun openTechLoginScreen() {
        runOrPostpone {
            getFragmentStack()?.push(TechLoginFragment())
        }
    }

    override fun openConnectionsSettingsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FmpSettingsFragment())
        }
    }

    override fun openPinCodeScreen(requestCode: Int, message: String) {
        runOrPostpone {
            getFragmentStack()?.push(PinCodeFragment.create(requestCode, message))
        }
    }

    override fun openPinCodeForTestEnvironment() {
        runOrPostpone {
            getFragmentStack()?.push(
                    PinCodeFragment.create(
                            SelectOperModeViewModel.REQUEST_CODE_TEST_ENVIRONMENT,
                            context.getString(R.string.tv_test_envir)))
        }
    }

    override fun openSelectOperModeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SelectOperModeFragment())
        }
    }

    override fun openPrinterChangeScreen() {
        runOrPostpone {
            getFragmentStack()?.push(PrinterChangeFragment())
        }
    }

    override fun openSettingsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SettingsFragment())
        }
    }

    override fun openAuxiliaryMenuScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AuxiliaryMenuFragment())
        }
    }

    override fun openFailurePinCodeScreen(message: String) {
        runOrPostpone {
            getFragmentStack()?.push(FailurePinCodeFragment.create(message = message))
        }
    }

    override fun openExitConfirmationScreen() {
        runOrPostpone {
            getFragmentStack()?.push(ExitWithConfirmationFragment())
        }
    }


    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

fun ICoreNavigator.runOrPostpone(function: () -> Unit) {
    functionsCollector.executeFunction(function)
}

interface ICoreNavigator {
    val functionsCollector: FunctionsCollector
    fun goBackWithArgs(args: Bundle)
    fun goBackWithResultCode(code: Int)
    fun goBack()
    fun finishApp()
    fun openAlertScreen(message: String, pageNumber: String = "?")
    fun openAlertScreen(failure: Failure, pageNumber: String = "?")
    fun openSupportScreen()
    fun <Params> showProgress(useCase: UseCase<Any, Params>)
    fun showProgress(title: String)
    fun hideProgress()
    fun openTechLoginScreen()
    fun openConnectionsSettingsScreen()
    fun openPinCodeScreen(requestCode: Int, message: String)
    fun openPinCodeForTestEnvironment()
    fun openSelectOperModeScreen()
    fun openPrinterChangeScreen()
    fun openSettingsScreen()
    fun openAuxiliaryMenuScreen()
    fun openFailurePinCodeScreen(message: String)
    fun openExitConfirmationScreen()
}

class FunctionsCollector(private val needCollectLiveData: LiveData<Boolean>) {

    private val functions: MutableList<() -> Unit> = mutableListOf()

    init {
        needCollectLiveData.observeForever { needCollect ->
            if (!needCollect) {
                functions.map { it }.forEach {
                    it()
                    functions.remove(it)
                }
            }
        }
    }

    fun executeFunction(func: () -> Unit) {
        if (needCollectLiveData.value == true) {
            functions.add(func)
        } else {
            func()
        }
    }


}