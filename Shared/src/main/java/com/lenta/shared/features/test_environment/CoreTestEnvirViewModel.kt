package com.lenta.shared.features.test_environment

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener

abstract class CoreTestEnvirViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {
    val pinCode1: MutableLiveData<String> = MutableLiveData("")
    val pinCode2: MutableLiveData<String> = MutableLiveData("")
    val pinCode3: MutableLiveData<String> = MutableLiveData("")
    val pinCode4: MutableLiveData<String> = MutableLiveData("")
    abstract fun onClickBack()
    abstract fun onClickGoOver()
    abstract fun onBackPressed()

    override fun onOkInSoftKeyboard(): Boolean {
        onClickGoOver()
        return true
    }
}