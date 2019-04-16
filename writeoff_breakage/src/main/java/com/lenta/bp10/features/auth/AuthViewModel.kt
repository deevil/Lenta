package com.lenta.bp10.features.auth

import androidx.lifecycle.*
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.login.BaseAuthViewModel
import com.lenta.shared.features.login.LoginFieldsValidator
import com.lenta.shared.features.login.usecase.Auth
import com.lenta.shared.features.login.usecase.AuthParams
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject


class AuthViewModel : BaseAuthViewModel() {

    @Inject
    lateinit var auth: Auth
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var loginFieldsValidator: LoginFieldsValidator
    @Inject
    lateinit var failureInterpreter: IFailureInterpreter

    override val enterEnabled: MutableLiveData<Boolean> by lazy {
        login.value = ""
        password.value = ""
        login.combineLatest(password).map { loginFieldsValidator.isValid(it?.first, it?.second) }
    }


    override fun onClickEnter() {
        viewModelScope.launch {
            progress.value = true
            auth(AuthParams(login.value!!, password.value!!)).either(::handleFailure, ::handleAuthSuccess)
            progress.value = false

        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAllertScreen(message = failureInterpreter.getFailureDescription(failure))
    }



    private fun handleAuthSuccess(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        navigator.openSelectMarketScreen()

    }

    override fun onBackPressed() {
    }
}