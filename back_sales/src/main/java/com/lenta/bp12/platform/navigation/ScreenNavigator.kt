package com.lenta.bp12.platform.navigation

import android.content.Context
import com.lenta.bp12.R
import com.lenta.bp12.features.auth.AuthFragment
import com.lenta.bp12.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp12.features.main_menu.MainMenuFragment
import com.lenta.bp12.features.select_market.SelectMarketFragment
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.progress.IProgressUseCaseInformator
import javax.inject.Inject

class ScreenNavigator @Inject constructor(
        private val context: Context,
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


    // Основные экраны
    override fun openLoginScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AuthFragment())
        }
    }

    override fun openFastDataLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FastDataLoadingFragment())
        }
    }

    override fun openSelectMarketScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SelectMarketFragment())
        }
    }

    override fun openMainMenuScreen() {
        runOrPostpone {
            getFragmentStack()?.push(MainMenuFragment())
        }
    }


    // Информационные экраны
    override fun showUnsentDataFoundOnDevice(deleteCallback: () -> Unit, goOverCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.unsent_data_found_on_device),
                    pageNumber = "89",
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForButton3 = backFragmentResultHelper.setFuncForResult(deleteCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(goOverCallback),
                    buttonDecorationInfo3 = ButtonDecorationInfo.delete,
                    rightButtonDecorationInfo = ButtonDecorationInfo.goOver))
        }
    }

    override fun showTwelveCharactersEntered(sapCallback: () -> Unit, barCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.twelve_characters_entered),
                    pageNumber = "40",
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(sapCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(barCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.sap,
                    rightButtonDecorationInfo = ButtonDecorationInfo.barcode))
        }
    }

    override fun showUnsavedDataWillBeLost(yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.unsaved_data_will_be_lost),
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    pageNumber = "92",
                    leftButtonDecorationInfo = ButtonDecorationInfo.back,
                    rightButtonDecorationInfo = ButtonDecorationInfo.proceed
            )
            )
        }
    }





}

interface IScreenNavigator : ICoreNavigator {
    fun openFirstScreen()
    fun openLoginScreen()
    fun openFastDataLoadingScreen()
    fun openSelectMarketScreen()
    fun openMainMenuScreen()

    fun showUnsentDataFoundOnDevice(deleteCallback: () -> Unit, goOverCallback: () -> Unit)
    fun showTwelveCharactersEntered(sapCallback: () -> Unit, barCallback: () -> Unit)
    fun showUnsavedDataWillBeLost(yesCallback: () -> Unit)

}