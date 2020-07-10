package com.lenta.bp16.platform.navigation

import android.content.Context
import com.lenta.bp16.R
import com.lenta.bp16.platform.Constants
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.features.alert.AlertFragment
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

    override fun openAlertPartNotFound() {
        runOrPostpone {
            getFragmentStack()?.push(AlertFragment.create(
                    message = context.getString(R.string.tw_alert_part_not_found),
                    iconRes = R.drawable.ic_warning_red_80dp,
                    pageNumber = Constants.ALERT_FRAGMENT))
        }
    }

}

interface IScreenNavigator : ICoreNavigator {
    fun openAlertPartNotFound()
}