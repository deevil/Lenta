package com.lenta.shared.features.message

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.lifecycle.viewModelScope
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

    lateinit var message: String

    @DrawableRes
    var iconRes: Int = 0
    var codeConfirm: Int? = null

    init {
        viewModelScope.launch {
            if (codeConfirm == null) {
                goBackWithDelay(params = null)
            }

        }
    }

    open fun onClickApply() {
        codeConfirm?.let {
            coreNavigator.goBackWithArgs(
                    args = Bundle().apply {
                        putInt(KEY_ARGS_ID_CODE_CONFIRM, it)
                    })
        }


    }

    companion object {
        val KEY_ARGS_ID_CODE_CONFIRM by lazy { "KEY_ARGS_ID_CODE_CONFIRM" }
    }
}