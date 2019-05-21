package com.lenta.shared.platform.navigation

import android.os.Bundle
import com.lenta.shared.platform.activity.ForegroundActivityProvider


class CoreNavigator constructor(private val foregroundActivityProvider: ForegroundActivityProvider) : ICoreNavigator {
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

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

interface ICoreNavigator {
    fun goBackWithArgs(args: Bundle)
    fun goBack()
    fun finishApp()
}