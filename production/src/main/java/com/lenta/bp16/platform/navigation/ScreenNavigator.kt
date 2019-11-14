package com.lenta.bp16.platform.navigation

import android.content.Context
import com.lenta.bp16.features.auth.AuthFragment
import com.lenta.bp16.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp16.features.main_menu.MainMenuFragment
import com.lenta.bp16.features.select_market.SelectMarketFragment
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
import com.lenta.shared.progress.IProgressUseCaseInformator
import javax.inject.Inject

class ScreenNavigator @Inject constructor(
        private val context: Context,
        private val coreNavigator: ICoreNavigator,
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator,
        private val progressUseCaseInformator: IProgressUseCaseInformator
) : IScreenNavigator, ICoreNavigator by coreNavigator {

    override fun openFirstScreen() {
        if (authenticator.isAuthorized()) {
            openMainMenuScreen()
        } else {
            openLoginScreen()
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

    override fun openFastDataLoadingScreen() {
        runOrPostpone {
            getFragmentStack()?.push(FastDataLoadingFragment())
        }
    }

    override fun openLoginScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AuthFragment())
        }
    }


    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack


    // Информационные экраны
    /*override fun showConfirmPriceTagsPrinting(priceTagNumber: Int, confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(message = context.getString(R.string.confirm_price_tags_printing, priceTagNumber),
                    pageNumber = "10",
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm))
        }
    }

    override fun showPrintPriceOffer(goodName: String, noCallback: () -> Unit, yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.print_price_tag_for_good, goodName),
                    pageNumber = "43",
                    iconRes = R.drawable.ic_question_80dp,
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(noCallback),
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.no,
                    rightButtonDecorationInfo = ButtonDecorationInfo.yes
            ))
        }
    }*/

}

interface IScreenNavigator : ICoreNavigator {

    fun openFirstScreen()
    fun openSelectMarketScreen()
    fun openMainMenuScreen()
    fun openLoginScreen()
    fun openFastDataLoadingScreen()

    //fun showConfirmPriceTagsPrinting(priceTagNumber: Int, confirmCallback: () -> Unit)
    //fun showPrintPriceOffer(goodName: String, noCallback: () -> Unit, yesCallback: () -> Unit)

}