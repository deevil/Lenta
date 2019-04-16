package com.lenta.shared.platform.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lenta.shared.exception.Failure

abstract class CoreViewModel : ViewModel() {
    var failure: MutableLiveData<Failure> = MutableLiveData()

    open fun handleFailure(failure: Failure) {
        this.failure.value = failure
    }
}