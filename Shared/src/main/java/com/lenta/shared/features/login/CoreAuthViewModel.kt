package com.lenta.shared.features.login

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel

abstract class CoreAuthViewModel : CoreViewModel() {
    val login: MutableLiveData<String> = MutableLiveData("")
    val password: MutableLiveData<String> = MutableLiveData("")
    val appTitle: MutableLiveData<String> = MutableLiveData("")
    val progress: MutableLiveData<Boolean> = MutableLiveData(false)
    val skipButtonEnabled = MutableLiveData<Boolean>(true)
    abstract val enterEnabled: MutableLiveData<Boolean>
    abstract fun onClickEnter()
    abstract fun onClickAuxiliaryMenu()
    abstract fun onResume()
    fun onScanResult(data: String) {
        login.postValue(data)
    }
}