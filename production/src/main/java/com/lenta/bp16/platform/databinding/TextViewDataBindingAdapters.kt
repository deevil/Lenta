package com.lenta.bp16.platform.databinding

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.lenta.shared.utilities.extentions.setVisible
import com.lenta.shared.utilities.extentions.setVisibleGone

@BindingAdapter("hideEmpty")
fun hideEmpty(textView: TextView, text: String?) {
    textView.setVisible(!text.isNullOrEmpty())
}