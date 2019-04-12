package com.lenta.bp10.platform.navigation

import com.lenta.shared.platform.activity.ForegroundActivityProvider

class ScreenNavigator(private val foregroundActivityProvider: ForegroundActivityProvider) : IScreenNavigator {
    override fun openLoginScreen() {
        //getFragmentStack()?.push()
    }

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

interface IScreenNavigator {
    fun openLoginScreen()
}