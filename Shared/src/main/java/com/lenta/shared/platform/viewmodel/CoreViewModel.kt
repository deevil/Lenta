package com.lenta.shared.platform.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lenta.shared.exception.Failure
import com.lenta.shared.utilities.Logg

abstract class CoreViewModel : ViewModel() {
    var failure: MutableLiveData<Failure> = MutableLiveData()

    open fun handleFailure(failure: Failure) {
        Logg.e { "handleFailure: $failure" }
        this.failure.value = failure
    }
}