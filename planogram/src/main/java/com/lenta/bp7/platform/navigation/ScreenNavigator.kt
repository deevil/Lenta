package com.lenta.bp7.platform.navigation

import android.content.Context
import com.lenta.bp7.features.auth.AuthFragment
import com.lenta.bp7.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp7.features.option.OptionFragment
import com.lenta.bp7.features.select_market.SelectMarketFragment

import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
import com.lenta.shared.progress.IProgressUseCaseInformator

class ScreenNavigator(
        private val context: Context,
        private val coreNavigator: ICoreNavigator,
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator,
        private val progressUseCaseInformator: IProgressUseCaseInformator
) : IScreenNavigator, ICoreNavigator by coreNavigator {

    override fun closeAllScreen() {
        runOrPostpone {
            getFragmentStack()?.popAll()
        }
    }

    override fun openFirstScreen() {
        if (authenticator.isAuthorized()) {
            openMainMenuScreen()
        } else {
            openLoginScreen()
        }
    }

    override fun openLoginScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AuthFragment())
        }
    }

    override fun openSelectMarketScreen() {
        runOrPostpone {
            getFragmentStack()?.replace(SelectMarketFragment())
        }
    }

    override fun openFastDataLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FastDataLoadingFragment())
        }
    }

    override fun openOptionScreen() {
        runOrPostpone {
            getFragmentStack()?.push(OptionFragment())
        }
    }

    override fun openMainMenuScreen() {
        openNotImplementedScreenAlert("Главное меню")
    }


    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

}

interface IScreenNavigator : ICoreNavigator {
    fun closeAllScreen()
    fun openFirstScreen()
    fun openLoginScreen()
    fun openMainMenuScreen()
    fun openSelectMarketScreen()
    fun openFastDataLoadingScreen()
    fun openOptionScreen()

}