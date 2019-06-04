package com.lenta.inventory.features.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.repos.IRepoInMemoryHolder
import com.lenta.inventory.requests.network.PermissionsParams
import com.lenta.shared.utilities.runIfDebug
import com.lenta.inventory.requests.network.PermissionsRequest
import com.lenta.inventory.requests.network.PermissionsResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.login.CoreAuthViewModel
import com.lenta.shared.features.login.isEnterEnabled
import com.lenta.shared.features.login.isValidLoginFields
import com.lenta.shared.requests.network.Auth
import com.lenta.shared.requests.network.AuthParams
import com.lenta.shared.settings.IAppSettings
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
    @Inject
    lateinit var appSettings: IAppSettings
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    init {
        viewModelScope.launch {
            if (!appSettings.lastLogin.isNullOrEmpty()) {
                login.value = appSettings.lastLogin
            }
            runIfDebug {
                if (login.value.isNullOrEmpty()) {
                    login.value = "MAKAROV"
                }
                if (login.value == "MAKAROV") {
                    password.value = "1q2w3e4r"
                }

            }
        }
    }


    override val enterEnabled: MutableLiveData<Boolean> by lazy {
        login.combineLatest(password).map { isValidLoginFields(login = it?.first, password = it?.second) }
                .combineLatest(progress).map { isEnterEnabled(isFieldsValid = it?.first, inProgress = it?.second) }
    }


    override fun onClickEnter() {
        viewModelScope.launch {
            progress.value = true
            auth(AuthParams(login.value!!, password.value!!)).either(::handleFailure, ::loadPermissions)
        }
    }

    private fun loadPermissions(@Suppress("UNUSED_PARAMETER") boolean: Boolean) {
        viewModelScope.launch {
            progress.value = true
            permissionsRequest(PermissionsParams(login = "")).either(::handleFailure, ::handleAuthSuccess)
            progress.value = false
        }
    }

    private fun handleAuthSuccess(permissionsResult: PermissionsResult) {

        repoInMemoryHolder.permissionsResult = permissionsResult

        login.value.let {
            sessionInfo.userName = it
            appSettings.lastLogin = it
        }

        navigator.openSelectMarketScreen()
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        progress.value = false
        navigator.openAlertScreen(message = failureInterpreter.getFailureDescription(failure), pageNumber = "11/97")
    }


    override fun onClickAuxiliaryMenu() {
        navigator.openAuxiliaryMenuScreen()
    }
}