package com.lenta.shared.features.login

import androidx.lifecycle.MutableLiveData

class AuthFormUIModel {
    val login: MutableLiveData<String> = MutableLiveData()
    val password: MutableLiveData<String> = MutableLiveData()
    val progress: MutableLiveData<Boolean> = MutableLiveData()
}