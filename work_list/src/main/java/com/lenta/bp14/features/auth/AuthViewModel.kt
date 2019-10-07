package com.lenta.bp14.features.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.user_permitions.PermissionsRequestParams
import com.lenta.bp14.requests.user_permitions.UserPermissionsNetRequest
import com.lenta.shared.utilities.runIfDebug
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.login.CoreAuthViewModel
import com.lenta.shared.features.login.isEnterEnabled
import com.lenta.shared.features.login.isValidLoginFields
import com.lenta.shared.requests.network.Auth
import com.lenta.shared.requests.network.AuthParams
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.getBaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel : CoreAuthViewModel() {

    @Inject
    lateinit var auth: Auth
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var appSettings: IAppSettings
    @Inject
    lateinit var userPermissionsNetRequest: UserPermissionsNetRequest


    val packageName = MutableLiveData<String>()

    override val enterEnabled: MutableLiveData<Boolean> by lazy {
        login.combineLatest(password).map { isValidLoginFields(login = it?.first, password = it?.second) }
                .combineLatest(progress).map { isEnterEnabled(isFieldsValid = it?.first, inProgress = it?.second) }
    }

    val skipButtonEnabled = progress.map { it != true }

    init {
        viewModelScope.launch {
            //TODO - implement existUnsavedData
            sessionInfo.existUnsavedData = false
            sessionInfo.isAuthSkipped.value = false
            sessionInfo.packageName = packageName.value
        }
    }

    override fun onClickEnter() {
        viewModelScope.launch {
            progress.value = true
            auth(AuthParams(getLogin(), getPassword())).either(::handleFailure, ::loadPermissions)
        }
    }

    private fun loadPermissions(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        viewModelScope.launch {
            getLogin().let { login ->
                sessionInfo.userName = login
                sessionInfo.basicAuth = getBaseAuth(login, getPassword())
                appSettings.lastLogin = login
                if (sessionInfo.isAuthSkipped.value != true) {
                    userPermissionsNetRequest(PermissionsRequestParams(
                            userName = login
                    )).either(::handleFailure) {
                        onAuthSuccess(login)
                    }
                } else {
                    onAuthSuccess(login)
                }


            }

        }
    }

    private fun onAuthSuccess(login: String) {
        progress.value = false
        navigator.openFastDataLoadingScreen()
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        sessionInfo.isAuthSkipped.value = false
        progress.value = false
        navigator.openAlertScreen(failure, pageNumber = "97")
    }

    override fun onClickAuxiliaryMenu() {
        navigator.openAuxiliaryMenuScreen()
    }

    private fun getLogin(): String {
        return login.value?.trim() ?: ""
    }

    private fun getPassword(): String {
        return password.value?.trim() ?: ""
    }

    override fun onResume() {
        viewModelScope.launch {
            if (!appSettings.lastLogin.isNullOrEmpty()) {
                login.value = appSettings.lastLogin
            }
            runIfDebug {
                Logg.d { "login.value ${login.value}" }
                if (login.value.isNullOrEmpty()) {
                    login.value = "MAKAROV"
                }
                if (login.value == "MAKAROV" && getPassword().isEmpty()) {
                    password.value = "1q2w3e4r"
                }

            }
        }
    }

    fun onClickSkip() {
        login.value = appSettings.techLogin
        password.value = appSettings.techPassword
        sessionInfo.isAuthSkipped.value = true

        onClickEnter()
    }

}