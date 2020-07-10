package com.lenta.bp18.platform.navigation

import android.content.Context
import com.lenta.bp18.R
import com.lenta.bp18.features.auth.AuthFragment
import com.lenta.bp18.features.goods_info.GoodsInfoFragment
import com.lenta.bp18.features.select_goods.SelectGoodsFragment
import com.lenta.bp18.features.select_market.SelectMarketFragment
import com.lenta.bp18.platform.Constants
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.alert.AlertFragment
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

    override fun openGoodsInfoScreen() {
        runOrPostpone {
            getFragmentStack()?.push(GoodsInfoFragment())
        }
    }

    override fun openSelectGoodsScreen() {
        runOrPostpone {
            getFragmentStack()?.push(SelectGoodsFragment())
        }
    }

    // Informational screens
    override fun openAlertConfirmOpeningPackage() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.tw_unpucking),
                    iconRes = R.drawable.ic_question_yellow_80dp,
                    pageNumber = Constants.ALERT_SCREEN_NUMBER
            ))
        }
    }

    override fun openAlertSuccessfulOpeningPackage() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.tw_unpucking_success),
                    iconRes = R.drawable.ic_info_green_80dp,
                    pageNumber = Constants.ALERT_SCREEN_NUMBER

            ))
        }
    }

    override fun openAlertPartcodeNotFound() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.tw_part_number_not_found),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    pageNumber = Constants.ALERT_SCREEN_NUMBER
            ))
        }
    }

}

interface IScreenNavigator : ICoreNavigator {
    fun openAuthScreen()
    fun openSelectMarketScreen()
    fun openGoodsInfoScreen()
    fun openSelectGoodsScreen()
    fun openAlertConfirmOpeningPackage()
    fun openAlertSuccessfulOpeningPackage()
    fun openAlertPartcodeNotFound()
}