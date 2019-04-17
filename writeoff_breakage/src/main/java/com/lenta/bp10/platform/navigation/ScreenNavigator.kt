package com.lenta.bp10.platform.navigation

import com.lenta.bp10.features.alert.AlertFragment
import com.lenta.bp10.features.auth.AuthFragment
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.select_market.SelectMarketFragment
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.CustomAnimation
import com.lenta.shared.platform.navigation.IGoBackNavigator
import com.lenta.shared.utilities.Logg

class ScreenNavigator(
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator
) : IScreenNavigator {
    override fun openAlertScreen(message: String) {
        getFragmentStack()?.push(AlertFragment.create(message), CustomAnimation.vertical())
    }

    override fun goBack() {
        getFragmentStack()?.pop()
    }

    override fun openSelectMarketScreen() {
        getFragmentStack()?.push(SelectMarketFragment())
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

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

interface IScreenNavigator : IGoBackNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openSelectMarketScreen()
    fun openAlertScreen(message: String)
}