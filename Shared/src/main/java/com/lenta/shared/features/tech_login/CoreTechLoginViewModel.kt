package com.lenta.shared.features.tech_login

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel

abstract class CoreTechLoginViewModel : CoreViewModel() {
    val login: MutableLiveData<String> = MutableLiveData("")
    val password: MutableLiveData<String> = MutableLiveData("")
    abstract fun onClickBack()
    abstract fun onClickApp()
    abstract fun onBackPressed()
}
