package com.lenta.shared.platform.navigation

import android.os.Bundle
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.platform.activity.ForegroundActivityProvider


class CoreNavigator constructor(private val foregroundActivityProvider: ForegroundActivityProvider, private val failureInterpreter: IFailureInterpreter) : ICoreNavigator {
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

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

interface ICoreNavigator {
    fun goBackWithArgs(args: Bundle)
    fun goBack()
    fun finishApp()
    fun openAlertScreen(message: String)
    fun openAlertScreen(failure: Failure)
}