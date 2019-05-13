package com.lenta.bp10.features.test_environment

import androidx.lifecycle.viewModelScope
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.network.PinCodeInfo
import com.lenta.bp10.requests.network.PinCodeNetRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.test_environment.CoreTestEnvirViewModel
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.launch
import javax.inject.Inject

class TestEnvirViewModel : CoreTestEnvirViewModel() {

    @Inject
    lateinit var pinCodeNetRequest: PinCodeNetRequest

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    override fun onClickBack() {
        screenNavigator.goBack()
    }

    override fun onClickGoOver() {
        viewModelScope.launch {
            screenNavigator.showProgress(pinCodeNetRequest)
            pinCodeNetRequest(null).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(pinCode: PinCodeInfo) {
        Logg.d { "handleSuccess $pinCode" }
        if (pinCode.pinCode == pinCode1.value+pinCode2.value+pinCode3.value+pinCode4.value) {
            screenNavigator.openAlertScreen("Пин-код верный")
        }
        else {
            screenNavigator.openAlertScreen("Пин-код неверный")
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }

    override fun onBackPressed() {
    }
}