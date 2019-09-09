package com.lenta.bp14.util

import android.widget.EditText
import androidx.databinding.BindingAdapter
import com.redmadrobot.inputmask.MaskedTextChangedListener

@BindingAdapter("maskPattern")
fun setMaskPattern(editText: EditText, mask: String) {
    val listener = MaskedTextChangedListener(mask, editText)
    editText.addTextChangedListener(listener)
    editText.onFocusChangeListener = listener
}