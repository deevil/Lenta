package com.lenta.shared.features.tech_login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.network.PinCodeInfo
import com.lenta.shared.requests.network.PinCodeNetRequest
import com.lenta.shared.requests.network.PinCodeRequestParams
import com.lenta.shared.settings.IAppSettings
import kotlinx.coroutines.launch
import javax.inject.Inject

class TechLoginViewModel : CoreViewModel() {
    @Inject
    lateinit var screenNavigator: ICoreNavigator

    @Inject
    lateinit var pinCodeNetRequest: PinCodeNetRequest

    @Inject
    lateinit var settings: IAppSettings

    val login: MutableLiveData<String> = MutableLiveData("")
    val password: MutableLiveData<String> = MutableLiveData("")


    fun onClickApply() {

        viewModelScope.launch {
            screenNavigator.showProgress(pinCodeNetRequest)
            pinCodeNetRequest(PinCodeRequestParams(
                    login = login.value ?: "",
                    password = password.value ?: ""
            )).either(::handleFailure, ::handlePinCodeSuccess)
            screenNavigator.hideProgress()
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }

    private fun handlePinCodeSuccess(pinCodeInfo: PinCodeInfo) {
        settings.techLogin = login.value!!
        settings.techPassword = password.value!!

        screenNavigator.goBack()

    }


}
