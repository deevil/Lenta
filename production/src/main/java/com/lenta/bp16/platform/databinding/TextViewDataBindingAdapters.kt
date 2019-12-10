package com.lenta.bp16.platform.databinding

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.lenta.shared.utilities.extentions.setVisible
import com.lenta.shared.utilities.extentions.setVisibleGone

@BindingAdapter("goneEmptyLine")
fun setGoneEmptyLine(textView: TextView, text: String?) {
    if (text.isNullOrEmpty()) {
        textView.setVisibleGone()
    } else {
        textView.setVisible()
    }
}