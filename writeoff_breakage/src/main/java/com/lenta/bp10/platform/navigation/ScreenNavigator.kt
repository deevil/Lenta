package com.lenta.bp10.platform.navigation

import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.login.LoginFragment
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.utilities.Logger
import javax.inject.Inject

class ScreenNavigator
@Inject constructor(
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator
) : IScreenNavigator {

    override fun openFirsctScreen() {
        if (authenticator.isAuthorized()) {

        } else {
            openLoginScreen()
        }
    }

    override fun openLoginScreen() {
        Logger.d()
        getFragmentStack()?.push(LoginFragment())
    }

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

interface IScreenNavigator {
    fun openFirsctScreen()
    fun openLoginScreen()
}