package com.lenta.shared.utilities.databinding

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.lenta.shared.R
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.extentions.setTextViewDrawableColor
import com.lenta.shared.utilities.extentions.setVisible

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

@BindingAdapter(value = ["setTextWithVisibilities", "prefix", "postfix"], requireAll = false)
fun setTextWithVisibilities(textView: TextView, text: String?, prefix: String?, postfix: String?) {
    textView.setVisible(!text.isNullOrEmpty())
    val resText = "${prefix ?: ""}${text ?: ""}${postfix ?: ""}"
    textView.text = resText
}

@BindingAdapter(value = ["intWithVisibilities", "prefix", "postfix"], requireAll = false)
fun intWithVisibilities(textView: TextView, counter: Int?, prefix: String?, postfix: String?) {
    var resText: String = ""
    counter?.let {
        resText = if (it > 0) it.toString() else ""
    }
    setTextWithVisibilities(textView, text = resText, prefix = prefix, postfix = postfix)
}