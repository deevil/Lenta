package com.lenta.bp9.features.auth

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.PermissionsGrzRequest
import com.lenta.bp9.requests.network.PermissionsGrzResult
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
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.getBaseAuth
import com.lenta.shared.utilities.runIfDebug
import javax.inject.Inject


class AuthViewModel : CoreAuthViewModel() {

    @Inject
    lateinit var auth: Auth
    @Inject
    lateinit var permissionsGrzRequest: PermissionsGrzRequest
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var appSettings: IAppSettings
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    val msgUserNoRights: MutableLiveData<String> = MutableLiveData()

    init {
        launchUITryCatch {
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
        launchUITryCatch {
            progress.value = true
            auth(AuthParams(login.value!!, password.value!!)).either(::handleFailure, ::loadPermissions)
        }
    }

    private fun loadPermissions(@Suppress("UNUSED_PARAMETER") boolean: Boolean) {

        getLogin().let {
            sessionInfo.userName = it
            sessionInfo.basicAuth = getBaseAuth(it, getPassword())
            appSettings.lastLogin = it
        }

        launchUITryCatch {
            progress.value = true
            permissionsGrzRequest(null).either(::handleFailure, ::handleAuthSuccess)
            progress.value = false
        }
    }

    private fun handleAuthSuccess(permissionsGrzResult: PermissionsGrzResult) {

        if (permissionsGrzResult.markets.isEmpty()) {
            navigator.openAlertNotPermissions(String.format(msgUserNoRights.value!!, getLogin()))
            return
        }

        repoInMemoryHolder.permissions = permissionsGrzResult

        navigator.openSelectMarketScreen()
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        progress.value = false
        navigator.openAlertScreen(failure, pageNumber = "96")
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
                    login.value = "MAKAROV"
                }
                if (login.value == "MAKAROV" && getPassword().isEmpty()) {
                    password.value = "1q2w3e4r"
                }

            }
        }
    }


}