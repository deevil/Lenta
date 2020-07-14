package com.lenta.bp18.features.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp18.platform.Constants
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IRepoInMemoryHolder
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.login.CoreAuthViewModel
import com.lenta.shared.features.login.isEnterEnabled
import com.lenta.shared.features.login.isValidLoginFields
import com.lenta.shared.requests.network.Auth
import com.lenta.shared.requests.network.AuthParams
import com.lenta.shared.requests.network.StoresRequest
import com.lenta.shared.requests.network.StoresRequestResult
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.getBaseAuth
import com.lenta.shared.utilities.runIfDebug
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
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var storesRequest: StoresRequest

    val packageName = MutableLiveData<String>()

    override val enterEnabled: MutableLiveData<Boolean> by lazy {
        login.combineLatest(password).map { isValidLoginFields(login = it?.first, password = it?.second) }
                .combineLatest(progress).map { isEnterEnabled(isFieldsValid = it?.first, inProgress = it?.second) }
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
            }
            progress.value = true
            storesRequest(null).either(::handleFailure, ::onAuthSuccess)
            progress.value = false
        }
    }

    private fun onAuthSuccess(storesRequestResult: StoresRequestResult) {
        repoInMemoryHolder.storesRequestResult = storesRequestResult
        navigator.openSelectMarketScreen()
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        progress.value = false
        navigator.openAlertScreen(failure, pageNumber = Constants.ALERT_SCREEN_NUMBER)
    }

    override fun onClickAuxiliaryMenu() {
        navigator.openAuxiliaryMenuScreen()
    }

    private fun getLogin(): String {
        return login.value?.trim().orEmpty()
    }

    private fun getPassword(): String {
        return password.value?.trim().orEmpty()
    }

    override fun onResume() {
        viewModelScope.launch {
            if (!appSettings.lastLogin.isNullOrEmpty()) {
                login.value = appSettings.lastLogin
            }
            runIfDebug {
                Logg.d { "login.value ${login.value}" }
                if (login.value.isNullOrEmpty()) {
                    login.value = Constants.USER_200
                }
                if (login.value == Constants.USER_200 && getPassword().isEmpty()) {
                    password.value = Constants.TEST_PASSWORD
                }
            }
        }
    }

}