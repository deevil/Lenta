package com.lenta.shared.features.test_environment

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.network.PinCodeInfo
import com.lenta.shared.requests.network.PinCodeNetRequest
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class PinCodeViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {
    var requestCode: Int? = null
    val message: MutableLiveData<String> = MutableLiveData("")
    val pinCode1: MutableLiveData<String> = MutableLiveData("")
    val pinCode2: MutableLiveData<String> = MutableLiveData("")
    val pinCode3: MutableLiveData<String> = MutableLiveData("")
    val pinCode4: MutableLiveData<String> = MutableLiveData("")


    @Inject
    lateinit var pinCodeNetRequest: PinCodeNetRequest

    @Inject
    lateinit var screenNavigator: ICoreNavigator


    fun onClickGoOver() {
        viewModelScope.launch {
            screenNavigator.showProgress(pinCodeNetRequest)
            pinCodeNetRequest(null).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(pinCode: PinCodeInfo) {
        Logg.d { "handleSuccess $pinCode" }
        if (pinCode.pinCode == pinCode1.value + pinCode2.value + pinCode3.value + pinCode4.value) {
            screenNavigator.goBackWithArgs(Bundle().apply {
                putInt(KEY_ARGS_ID_CODE_CONFIRM, requestCode ?: 0)
            })
        } else {
            screenNavigator.openAlertScreen("Пин-код неверный")
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }


    override fun onOkInSoftKeyboard(): Boolean {
        onClickGoOver()
        return true
    }

    companion object {
        val KEY_ARGS_ID_CODE_CONFIRM by lazy { "KEY_ARGS_ID_CODE_CONFIRM" }
    }
}