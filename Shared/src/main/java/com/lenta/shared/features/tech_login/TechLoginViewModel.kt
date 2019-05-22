package com.lenta.shared.features.tech_login

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class TechLoginViewModel : CoreViewModel() {
    val login: MutableLiveData<String> = MutableLiveData("")
    val password: MutableLiveData<String> = MutableLiveData("")

    @Inject
    lateinit var screenNavigator: ICoreNavigator


    fun onClickApp() {
        //TODO смены тех.пользователя
        screenNavigator.goBack()
    }


}
