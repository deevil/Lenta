package com.lenta.shared.utilities.databinding

import android.widget.Button
import androidx.databinding.BindingAdapter

@BindingAdapter("isDisable")
fun setDisable(button: Button, isDisable: Boolean) {
    button.isEnabled = !isDisable
}