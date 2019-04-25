package com.lenta.shared.features.login

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel

abstract class CoreAuthViewModel : CoreViewModel() {
    val login: MutableLiveData<String> = MutableLiveData("borisenko")
    val password: MutableLiveData<String> = MutableLiveData("123456")
    val progress: MutableLiveData<Boolean> = MutableLiveData(false)
    abstract val enterEnabled: MutableLiveData<Boolean>
    abstract fun onClickEnter()
    abstract fun onBackPressed()
    abstract fun onClickAuxiliaryMenu()
}