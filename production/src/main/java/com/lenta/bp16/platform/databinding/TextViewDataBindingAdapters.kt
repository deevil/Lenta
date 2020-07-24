package com.lenta.bp16.platform.databinding

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.lenta.shared.utilities.extentions.setVisible

@BindingAdapter("hideEmpty")
fun hideEmpty(textView: TextView, text: String?) {
    textView.setVisible(!text.isNullOrEmpty())
}