package com.lenta.movement.features.auth

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.movement.repos.IRepoInMemoryHolder
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
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.getBaseAuth
import com.lenta.shared.utilities.runIfDebug
import javax.inject.Inject

class AuthViewModel: CoreAuthViewModel() {

    @Inject
    lateinit var auth: Auth
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var appSettings: IAppSettings
    @Inject
    lateinit var storesRequest: StoresRequest

    override val enterEnabled: MutableLiveData<Boolean> by lazy {
        login.combineLatest(password).map { isValidLoginFields(login = it?.first, password = it?.second) }
            .combineLatest(progress).map { isEnterEnabled(isFieldsValid = it?.first, inProgress = it?.second) }
    }


    override fun onClickEnter() {
        launchUITryCatch {
            progress.value = true
            auth(AuthParams(getLogin(), getPassword())).either(::handleFailure, ::loadPermissions)
        }
    }

    private fun loadPermissions(@Suppress("UNUSED_PARAMETER") boolean: Boolean) {
        launchUITryCatch {
            getLogin().let {
                sessionInfo.userName = it
                sessionInfo.basicAuth = getBaseAuth(it, getPassword())
                appSettings.lastLogin = it
            }
            progress.value = true
            storesRequest(null).either(::handleFailure, ::handleAuthSuccess)
            progress.value = false
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        progress.value = false
        navigator.openAlertScreen(failure, pageNumber = PAGE_NUMBER)
    }


    private fun handleAuthSuccess(storesRequestResult: StoresRequestResult) {
        repoInMemoryHolder.storesRequestResult = storesRequestResult
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
        launchUITryCatch {
            if (!appSettings.lastLogin.isNullOrEmpty()) {
                login.value = appSettings.lastLogin
            }
            runIfDebug {
                Logg.d { "login.value ${login.value}" }
                if (login.value.isNullOrEmpty()) {
                    login.value = LOGIN
                }
                if (login.value == LOGIN && getPassword().isEmpty()) {
                    password.value = PASSWORD
                }
            }
        }
    }

    companion object {
        private const val PAGE_NUMBER = "93"
        private const val LOGIN = "MAKAROV"
        private const val PASSWORD = "1q2w3e4r"
    }
}