package com.lenta.shared.features.message

import androidx.annotation.DrawableRes
import com.lenta.shared.features.message.usecase.DelayGoBack
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

open class MessageViewModel : CoreViewModel() {


    @Inject
    lateinit var goBackWithDelay: DelayGoBack

    @Inject
    lateinit var coreNavigator: ICoreNavigator

    lateinit var message: String

    @DrawableRes
    var iconRes: Int = 0
    var codeConfirm: Int? = null

    init {
        /*viewModelScope.launch {
            if (codeConfirm == null) {
                goBackWithDelay(params = null)
            }

        }*/
    }

    open fun onClickApply() {
        codeConfirm?.let {
            coreNavigator.goBackWithResultCode(it)
        }
    }

    fun onClickLeftButton() {
        coreNavigator.goBack()
    }


}


