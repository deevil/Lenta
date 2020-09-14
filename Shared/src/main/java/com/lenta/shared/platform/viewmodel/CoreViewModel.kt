package com.lenta.shared.platform.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.livedata.SingleLiveEvent
import com.lenta.shared.platform.navigation.BackFragmentResultHelper
import com.lenta.shared.utilities.Logg
import javax.inject.Inject

abstract class CoreViewModel : ViewModel() {

    @Inject
    lateinit var backFragmentResultHelper: BackFragmentResultHelper


    val failure = SingleLiveEvent<Failure>()

    val selectedPage = MutableLiveData(0)

    open fun handleFailure(failure: Failure) {
        Logg.e { "handleFailure: $failure" }
        this.failure.postValue(failure)
    }

    open fun handleFragmentResult(code: Int?) : Boolean {
        backFragmentResultHelper.getFuncAndClear(code)?.let {
            it()
            return true
        }
        return false
    }

}