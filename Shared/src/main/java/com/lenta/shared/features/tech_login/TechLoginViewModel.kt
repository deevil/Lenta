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
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
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
    val applyButtonEnabled: MutableLiveData<Boolean> = login.combineLatest(password).map { pair ->
        pair?.let {
            return@map !it.first.isNullOrBlank() && !it.second.isNullOrBlank()
        }
    }

    private lateinit var prefixScreen: String
    fun setPrefixScreen(prefixScreen: String){
        this.prefixScreen = prefixScreen
    }


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
        screenNavigator.openAlertScreen(failure, pageNumber = "$prefixScreen/97")
    }

    private fun handlePinCodeSuccess(pinCodeInfo: PinCodeInfo) {
        settings.techLogin = login.value!!
        settings.techPassword = password.value!!

        screenNavigator.goBack()

    }


}
