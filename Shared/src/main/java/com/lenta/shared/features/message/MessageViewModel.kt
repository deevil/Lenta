package com.lenta.shared.features.message

import androidx.annotation.DrawableRes
import androidx.lifecycle.viewModelScope
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.features.message.usecase.DelayGoBack
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

open class MessageViewModel : CoreViewModel() {

    @Inject
    lateinit var goBackWithDelay: DelayGoBack

    @Inject
    lateinit var coreNavigator: ICoreNavigator

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    lateinit var message: String

    @DrawableRes
    var iconRes: Int = 0
    var codeConfirm: Int? = null
    var codeConfirmForLeft: Int? = null
    var textColor: Int? = null
    var timeAutoExitInMillis: Int? = null

    init {

        viewModelScope.launch {
            timeAutoExitInMillis?.let { delayInMillis ->
                if (codeConfirm == null) {
                    goBackWithDelay(params = delayInMillis.toLong())
                }
            }

            analyticsHelper.infoScreenMessage(message)

        }

    }

    open fun onClickRightButton() {
        codeConfirm?.let {
            coreNavigator.goBackWithResultCode(it)
        }
    }

    fun onClickLeftButton() {
        codeConfirmForLeft?.let {
            coreNavigator.goBackWithResultCode(it)
            return
        }
        coreNavigator.goBack()
    }


}


