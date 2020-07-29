package com.lenta.bp18.platform.navigation

import android.content.Context
import android.os.Bundle
import com.lenta.bp18.R
import com.lenta.bp18.features.auth.AuthFragment
import com.lenta.bp18.features.good_info.GoodInfoFragment
import com.lenta.bp18.features.loading.fast.FastDataLoadingFragment
import com.lenta.bp18.features.select_good.SelectGoodFragment
import com.lenta.bp18.features.select_market.SelectMarketFragment
import com.lenta.bp18.platform.Constants
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.navigation.runOrPostpone
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.progress.IProgressUseCaseInformator
import com.lenta.shared.utilities.extentions.getAppInfo
import com.lenta.shared.utilities.extentions.getApplicationName
import javax.inject.Inject

class ScreenNavigator @Inject constructor(
        private val context: Context,
        private val coreNavigator: ICoreNavigator,
        private val foregroundActivityProvider: ForegroundActivityProvider,
        private val authenticator: IAuthenticator,
        private val progressUseCaseInformator: IProgressUseCaseInformator
) : IScreenNavigator, ICoreNavigator by coreNavigator {

    private fun getFragmentStack() = foregroundActivityProvider.getActivity()?.fragmentStack

    //Base screens
    override fun openAuthScreen() {
        runOrPostpone {
            getFragmentStack()?.push(AuthFragment())
        }
    }

    override fun openSelectMarketScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SelectMarketFragment())
        }
    }

    override fun openGoodsInfoScreen(goodInfo: Bundle, weight: String?) {
        runOrPostpone {
            getFragmentStack()?.push(GoodInfoFragment.newInstance(goodInfo, weight))
        }
    }

    override fun openFastDataLoadingScreen() {
        getFragmentStack()?.push(FastDataLoadingFragment())
    }

    override fun openSelectGoodScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SelectGoodFragment())
        }
    }


    // Informational screens
    override fun showConfirmOpeningPackage(noCallback: () -> Unit, yesCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.tw_unpucking),
                    title = context.getAppInfo(),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = Constants.CONFIRMATION_SCREEN,
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(noCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.back,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(yesCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm
            ))
        }
    }

    override fun showConfirmSaveData(backCallback: () -> Unit, confirmCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.tw_save_data),
                    title = context.getAppInfo(),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = Constants.CONFIRMATION_SCREEN,
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(backCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.back,
                    codeConfirmForRight = backFragmentResultHelper.setFuncForResult(confirmCallback),
                    rightButtonDecorationInfo = ButtonDecorationInfo.confirm
            ))
        }
    }

    override fun showAlertSuccessfulOpeningPackage(goOverCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.tw_unpucking_success),
                    title = context.getAppInfo(),
                    iconRes = R.drawable.ic_info_green_80dp,
                    pageNumber = Constants.ALERT_SCREEN_NUMBER,
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(goOverCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.back
            ))
        }
    }

    override fun showAlertPartCodeNotFound() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.tw_part_number_not_found),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    pageNumber = Constants.ALERT_SCREEN_NUMBER
            ))
        }
    }

    override fun showAlertGoodsNotFound() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.tw_good_not_found),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    pageNumber = Constants.ALERT_SCREEN_NUMBER,
                    timeAutoExitInMillis = Constants.TIME_OUT
            ))
        }
    }

    override fun showAlertServerNotAvailable(goOverCallback: () -> Unit) {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.tw_server_no_available),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    pageNumber = Constants.ALERT_SCREEN_NUMBER,
                    timeAutoExitInMillis = Constants.TIME_OUT,
                    codeConfirmForLeft = backFragmentResultHelper.setFuncForResult(goOverCallback),
                    leftButtonDecorationInfo = ButtonDecorationInfo.back
            ))
        }
    }

}

interface IScreenNavigator : ICoreNavigator {

    fun openAuthScreen()
    fun openSelectMarketScreen()
    fun openSelectGoodScreen()
    fun openGoodsInfoScreen(goodInfo: Bundle, weight: String?)
    fun openFastDataLoadingScreen()

    fun showConfirmOpeningPackage(noCallback: () -> Unit, yesCallback: () -> Unit)
    fun showConfirmSaveData(backCallback: () -> Unit, confirmCallback: () -> Unit)
    fun showAlertSuccessfulOpeningPackage(goOverCallback: () -> Unit)
    fun showAlertPartCodeNotFound()
    fun showAlertGoodsNotFound()
    fun showAlertServerNotAvailable(goOverCallback: () -> Unit)
}