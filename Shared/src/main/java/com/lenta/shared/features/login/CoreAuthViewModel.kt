package com.lenta.shared.features.login

import android.view.View
import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel

abstract class CoreAuthViewModel : CoreViewModel() {
    val login: MutableLiveData<String> = MutableLiveData("")
    val password: MutableLiveData<String> = MutableLiveData("")
    val appTitle: MutableLiveData<String> = MutableLiveData("")
    val progress: MutableLiveData<Boolean> = MutableLiveData(false)
    abstract val enterEnabled: MutableLiveData<Boolean>
    abstract fun onClickEnter()
    abstract fun onClickAuxiliaryMenu()
    abstract fun onResume()
    fun onScanResult(data: String, view: View?) {
        view?.let {v ->
            (v is EditText).let {
                if (it) {
                    (v as EditText).setText(data)
                    v.requestFocus()
                }
            }
        }
        //login.postValue(data)
    }
}