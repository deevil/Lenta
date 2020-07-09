package com.lenta.shared.features.tech_login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.network.PinCodeInfo
import com.lenta.shared.requests.network.PinCodeNetRequest
import com.lenta.shared.requests.network.PinCodeRequestParams
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.PackageName
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
    @Inject
    lateinit var sessionInfo: ISessionInfo


    val login: MutableLiveData<String> = MutableLiveData("")
    val password: MutableLiveData<String> = MutableLiveData("")

    val applyButtonEnabled: MutableLiveData<Boolean> = login.combineLatest(password).map { pair ->
        pair?.let {
            return@map !it.first.isNullOrBlank() && !it.second.isNullOrBlank()
        }
    }

    fun onClickApply() {
        viewModelScope.launch {
            when (sessionInfo.packageName) {
                PackageName.PLE.path -> {
                    if (sessionInfo.existUnsavedData == true) {
                        screenNavigator.showUnsavedDataDetected {
                            changeLoginPassword()
                        }
                    }
                }
                else -> changeLoginPassword()
            }
        }
    }

    private fun changeLoginPassword() {
        viewModelScope.launch {
            screenNavigator.showProgress(pinCodeNetRequest)
            pinCodeNetRequest(PinCodeRequestParams(
                    login = login.value.orEmpty(),
                    password = password.value.orEmpty()
            )).either(::handleFailure, ::handlePinCodeSuccess)
            screenNavigator.hideProgress()
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure, pageNumber = "97")
    }

    private fun handlePinCodeSuccess(@Suppress("UNUSED_PARAMETER") pinCodeInfo: PinCodeInfo) {
        settings.techLogin = login.value!!
        settings.techPassword = password.value!!

        screenNavigator.goBack()
    }

}
