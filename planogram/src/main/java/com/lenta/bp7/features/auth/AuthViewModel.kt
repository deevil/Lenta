package com.lenta.bp7.features.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IRepoInMemoryHolder
import com.lenta.bp7.requests.network.PermissionsParams
import com.lenta.bp7.requests.network.PermissionsRequest
import com.lenta.bp7.requests.network.PermissionsResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.login.CoreAuthViewModel
import com.lenta.shared.features.login.isEnterEnabled
import com.lenta.shared.features.login.isValidLoginFields
import com.lenta.shared.requests.network.Auth
import com.lenta.shared.requests.network.AuthParams
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.getBaseAuth
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
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var appSettings: IAppSettings
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder


    override val enterEnabled: MutableLiveData<Boolean> by lazy {
        login.combineLatest(password).map { isValidLoginFields(login = it?.first, password = it?.second) }
                .combineLatest(progress).map { isEnterEnabled(isFieldsValid = it?.first, inProgress = it?.second) }
    }


    override fun onClickEnter() {
        viewModelScope.launch {
            progress.value = true
            auth(AuthParams(getLogin(), getPassword())).either(::handleFailure, ::loadPermissions)
            progress.value = false
        }
    }

    private fun loadPermissions(@Suppress("UNUSED_PARAMETER") boolean: Boolean) {
        viewModelScope.launch {
            progress.value = true
            getLogin().let {
                sessionInfo.userName = it
                sessionInfo.basicAuth = getBaseAuth(it, getPassword())
                appSettings.lastLogin = it
            }
            permissionsRequest(PermissionsParams(login = getLogin())).either(::handleFailure, ::handleAuthSuccess)
            progress.value = false
        }

        //TODO возможно после авторизации необходимо загружать дополнительные данные. При необходимости добавить тут

        //handleAuthSuccess(true)
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        progress.value = false
        navigator.openAlertScreen(failure, pageNumber = "97")
    }


    private fun handleAuthSuccess(permissionsResult: PermissionsResult) {
        repoInMemoryHolder.permissionsResult = permissionsResult

        navigator.openSelectMarketScreen()
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
            login.value = appSettings.techLogin
            password.value = appSettings.techPassword
        }
    }
}