package com.lenta.shared.features.login

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.BaseViewModel

abstract class BaseAuthViewModel : BaseViewModel() {
    val login: MutableLiveData<String> = MutableLiveData()
    val password: MutableLiveData<String> = MutableLiveData()
    val progress: MutableLiveData<Boolean> = MutableLiveData()
    abstract val enterEnabled: MutableLiveData<Boolean>
    abstract fun onClickEnter()
    abstract fun onBackPressed()
}