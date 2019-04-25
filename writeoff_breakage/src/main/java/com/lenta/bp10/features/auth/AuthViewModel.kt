package com.lenta.bp10.features.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.network.PermissionsParams
import com.lenta.bp10.requests.network.PermissionsRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.login.CoreAuthViewModel
import com.lenta.shared.features.login.isEnterEnabled
import com.lenta.shared.features.login.isValidLoginFields
import com.lenta.shared.requests.network.Auth
import com.lenta.shared.requests.network.AuthParams
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject


class AuthViewModel : CoreAuthViewModel() {


    @Inject
    lateinit var auth: Auth
    @Inject
    lateinit var permissionsRequest: PermissionsRequest
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var failureInterpreter: IFailureInterpreter
    @Inject
    lateinit var sessionInfo: ISessionInfo

    override val enterEnabled: MutableLiveData<Boolean> by lazy {
        login.combineLatest(password).map { isValidLoginFields(login = it?.first, password = it?.second) }
                .combineLatest(progress).map { isEnterEnabled(isFieldsValid = it?.first, inProgress = it?.second) }
    }


    override fun onClickEnter() {
        viewModelScope.launch {
            progress.value = true
            auth(AuthParams(login.value!!, password.value!!)).either(::handleFailure, ::loadPermissions)
            progress.value = false
        }
    }

    private fun loadPermissions(@Suppress("UNUSED_PARAMETER") boolean: Boolean) {
        viewModelScope.launch {
            progress.value = true
            permissionsRequest(PermissionsParams(login = "")).either(::handleFailure, ::handleAuthSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(message = failureInterpreter.getFailureDescription(failure))
    }


    private fun handleAuthSuccess(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        sessionInfo.userName = login.value
        navigator.openSelectMarketScreen()

    }

    override fun onBackPressed() {
    }

    override fun onClickAuxiliaryMenu() {
        navigator.openAuxiliaryMenuScreen()
    }
}