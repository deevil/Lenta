package com.lenta.shared.features.message

import androidx.annotation.DrawableRes
import androidx.lifecycle.viewModelScope
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.features.message.usecase.DelayGoBack
import com.lenta.shared.features.message.usecase.GoBackParams
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
    var codeConfirmForExit: Int? = null
    var codeConfirmForRight: Int? = null
    var codeConfirmForButton2: Int? = null
    var codeConfirmForButton3: Int? = null
    var codeConfirmForButton4: Int? = null
    var codeConfirmForLeft: Int? = null
    var textColor: Int? = null
    var timeAutoExitInMillis: Int? = null

    init {

        viewModelScope.launch {
            timeAutoExitInMillis?.let { delayInMillis ->
                if (codeConfirmForRight == null) {
                    goBackWithDelay(params = GoBackParams(
                            timeInMillis = delayInMillis.toLong(),
                            codeForBackResult = codeConfirmForExit
                    ))
                }
            }

            analyticsHelper.infoScreenMessage(message)

        }

    }

    open fun onClickRightButton() {
        codeConfirmForRight?.let {
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

    fun onClickButton2() {
        codeConfirmForButton2?.let {
            coreNavigator.goBackWithResultCode(it)
        }
    }

    fun onClickButton3() {
        codeConfirmForButton3?.let {
            coreNavigator.goBackWithResultCode(it)
        }
    }

    fun onClickButton4() {
        codeConfirmForButton4?.let {
            coreNavigator.goBackWithResultCode(it)
        }
    }

    fun onBackPressed(): Boolean {
        if (codeConfirmForExit == null) {
            return true
        }
        coreNavigator.goBackWithResultCode(codeConfirmForExit!!)
        return false

    }


}


