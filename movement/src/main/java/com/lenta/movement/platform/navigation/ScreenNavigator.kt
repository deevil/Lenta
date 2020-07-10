package com.lenta.movement.platform.navigation

import android.content.Context
import com.lenta.movement.features.main_menu.MainMenuFragment
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
import com.lenta.shared.progress.IProgressUseCaseInformator
import javax.inject.Inject

class ScreenNavigator @Inject constructor(private val context: Context,
                                          private val coreNavigator: ICoreNavigator,
                                          private val foregroundActivityProvider: ForegroundActivityProvider,
                                          private val authenticator: IAuthenticator,
                                          private val progressUseCaseInformator: IProgressUseCaseInformator
) : IScreenNavigator, ICoreNavigator by coreNavigator {


    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack
    override fun openFirstScreen() {
        if (authenticator.isAuthorized()) {
            openMainMenuScreen()
        } else {
            openLoginScreen()
        }
    }

    override fun openLoginScreen() {
        TODO("Not yet implemented")
    }

    override fun openSelectMarketScreen() {
        TODO("Not yet implemented")
    }

    override fun openFastDataLoadingScreen() {
        TODO("Not yet implemented")
    }

    override fun openSelectionPersonnelNumberScreen(isScreenMainMenu: Boolean) {
        TODO("Not yet implemented")
    }

    override fun openMainMenuScreen() {
        runOrPostpone {
            getFragmentStack()?.push(MainMenuFragment())
        }
    }

}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openSelectMarketScreen()
    fun openFastDataLoadingScreen()
    fun openSelectionPersonnelNumberScreen(isScreenMainMenu: Boolean)
    fun openMainMenuScreen()
}