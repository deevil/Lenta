package com.lenta.shared.features.login

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel

abstract class CoreAuthViewModel : CoreViewModel() {
    val login: MutableLiveData<String> = MutableLiveData("")
    val password: MutableLiveData<String> = MutableLiveData("")
    val progress: MutableLiveData<Boolean> = MutableLiveData(false)
    abstract val enterEnabled: MutableLiveData<Boolean>
    abstract fun onClickEnter()
    abstract fun onBackPressed()
    abstract fun onClickSettings()
}