package com.lenta.shared.utilities.databinding

import android.view.KeyEvent
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter


@BindingAdapter(value = ["onOkInSoftKeyboard"])
fun setOnOkInSoftKeyboardListener(editText: EditText, onOkInSoftKeyboardListener: OnOkInSoftKeyboardListener?) {
    if (onOkInSoftKeyboardListener == null) {
        editText.setOnEditorActionListener(null)
        return
    }
    editText.setOnEditorActionListener { _: TextView?, _: Int?, _: KeyEvent? -> onOkInSoftKeyboardListener.onOkInSoftKeyboard() }

}

interface OnOkInSoftKeyboardListener {
    fun onOkInSoftKeyboard(): Boolean
}


