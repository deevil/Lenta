package com.lenta.bp15.platform.databinding

import android.widget.CheckBox
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData

@BindingAdapter(value = ["currentState", "clearOthersAfterChecked"])
fun setClearOthersAfterChecked(checkBox: CheckBox, currentState: MutableLiveData<Boolean>?, funcForClear: () -> Unit) {
    checkBox.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
            funcForClear.invoke()
            currentState?.value = true
        }
    }
}
