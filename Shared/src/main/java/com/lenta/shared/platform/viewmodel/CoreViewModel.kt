package com.lenta.shared.platform.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.navigation.BackFragmentResultHelper
import com.lenta.shared.utilities.Logg
import javax.inject.Inject

abstract class CoreViewModel : ViewModel() {
    var failure: MutableLiveData<Failure> = MutableLiveData()

    @Inject
    lateinit var backFragmentResultHelper: BackFragmentResultHelper

    open fun handleFailure(failure: Failure) {
        Logg.e { "handleFailure: $failure" }
        this.failure.value = failure
    }

    open fun handleFragmentResult(code: Int?) : Boolean {
        backFragmentResultHelper.getFuncAndClear(code)?.let {
            it()
            return true
        }
        return false
    }

}