package com.lenta.shared.platform.toolbar.bottom_toolbar

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.lenta.shared.R
import com.lenta.shared.utilities.extentions.setTextViewDrawableColor

@BindingAdapter(value = ["buttonDecorationInfo", "android:enabled"], requireAll = false)
fun setButtonDecorationInfo(textView: TextView, buttonDecorationInfo: ButtonDecorationInfo?, enabled: Boolean?) {
    if (buttonDecorationInfo == null) {
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        textView.text = null
        return
    }
    buttonDecorationInfo.iconRes.let {
        textView.setCompoundDrawablesWithIntrinsicBounds(0, buttonDecorationInfo.iconRes, 0, 0)
        if (buttonDecorationInfo.titleRes != 0) {
            textView.setText(buttonDecorationInfo.titleRes)
        }
    }
    if (enabled != null) {
        textView.isEnabled = enabled
        textView.setTextViewDrawableColor(if (enabled) R.color.color_text_white else R.color.color_disabled_blue)
    }
}