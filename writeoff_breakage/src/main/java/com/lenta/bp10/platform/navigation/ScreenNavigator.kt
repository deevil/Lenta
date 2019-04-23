package com.lenta.bp10.platform.navigation

import com.lenta.bp10.features.alert.AlertFragment
import com.lenta.bp10.features.auth.AuthFragment
import com.lenta.bp10.features.loading.fast.FastDataLoadingFragment
import com.lenta.shared.account.IAuthenticator
import com.lenta.bp10.features.select_market.SelectMarketFragment
import com.lenta.bp10.features.select_tab_number.SelectTabNumberFragment
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.CustomAnimation
import com.lenta.shared.platform.navigation.IGoBackNavigator
import com.lenta.shared.utilities.Logg

class ScreenNavigator(
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator
) : IScreenNavigator {
    override fun openAlertScreen(message: String, replace: Boolean) {
        getFragmentStack()?.let {
            val fragment = AlertFragment.create(message)
            if (replace) {
                it.replace(fragment)
            } else {
                it.push(fragment, CustomAnimation.vertical())
            }
        }


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
        getFragmentStack()?.push(SelectTabNumberFragment())
    }

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

interface IScreenNavigator : IGoBackNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openSelectMarketScreen()
    fun openAlertScreen(message: String, replace: Boolean = false)
    fun openFastDataLoadingScreen()
    fun openSelectionTabNumberScreen()
}