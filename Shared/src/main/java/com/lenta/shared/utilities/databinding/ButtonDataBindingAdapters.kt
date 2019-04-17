package com.lenta.shared.utilities.databinding

import android.widget.Button
import androidx.databinding.BindingAdapter

@BindingAdapter("isDisable")
fun bindIsGone(button: Button, isDisable: Boolean) {
    button.isEnabled = !isDisable
}