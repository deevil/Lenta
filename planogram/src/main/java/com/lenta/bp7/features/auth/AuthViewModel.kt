package com.lenta.bp7.features.auth

import androidx.lifecycle.MutableLiveData
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.login.CoreAuthViewModel
import com.lenta.shared.features.login.isEnterEnabled
import com.lenta.shared.features.login.isValidLoginFields
import com.lenta.shared.requests.network.Auth
import com.lenta.shared.requests.network.AuthParams
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.getBaseAuth
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


    val packageName: MutableLiveData<String> = MutableLiveData()

    init {
        launchUITryCatch {
            sessionInfo.packageName = packageName.value
        }
    }

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

    private fun loadPermissions(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        launchUITryCatch {
            getLogin().let { login ->
                sessionInfo.userName = login
                sessionInfo.basicAuth = getBaseAuth(login, getPassword())
                appSettings.lastLogin = login
            }

            progress.value = false

            navigator.openFastDataLoadingScreen()
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        progress.value = false
        navigator.openAlertScreen(failure, pageNumber = "97")
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
        launchUITryCatch {
            login.value = appSettings.techLogin
            password.value = appSettings.techPassword
        }
    }
}