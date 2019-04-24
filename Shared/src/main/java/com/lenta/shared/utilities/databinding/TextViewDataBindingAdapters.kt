package com.lenta.shared.utilities.databinding

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.lenta.shared.R
import com.lenta.shared.platform.battery_state.getIconForStatus
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.date_time.DateTimeUtil
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


@BindingAdapter(value = ["batteryLevel", "isCharging"])
fun setNetworkIsConnected(textView: TextView, batteryLevel: Int?, isCharging: Boolean?) {
    intWithVisibilities(textView, batteryLevel)
    textView.setCompoundDrawablesWithIntrinsicBounds(
            getIconForStatus(isCharging, batteryLevel),
            0, 0, 0)
}

@BindingAdapter(value = ["networkIsConnected"])
fun setNetworkIsConnected(textView: TextView, networkIsConnected: Boolean?) {
    textView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            if (networkIsConnected == true) R.drawable.ic_wifi_white_16dp else R.drawable.ic_signal_wifi_off_red_16dp,
            0)
}

@BindingAdapter(value = ["setTextWithVisibilities", "prefix", "postfix"], requireAll = false)
fun setTextWithVisibilities(textView: TextView, text: String?, prefix: String?, postfix: String?) {
    textView.setVisible(!text.isNullOrEmpty())
    val resText = "${prefix ?: ""}${text ?: ""}${postfix ?: ""}"
    textView.text = resText
}

@BindingAdapter(value = ["intWithVisibilities", "prefix", "postfix"], requireAll = false)
fun intWithVisibilities(textView: TextView, counter: Int?, prefix: String? = null, postfix: String? = null) {
    var resText: String = ""
    counter?.let {
        resText = if (it > -1) it.toString() else ""
    }
    setTextWithVisibilities(textView, text = resText, prefix = prefix, postfix = postfix)
}

@BindingAdapter(value = ["floatWithVisibilities", "prefix", "postfix"], requireAll = false)
fun floatWithVisibilities(textView: TextView, value: Float?, prefix: String? = null, postfix: String? = null) {
    var resText: String = ""
    value?.let {
        resText = if (it >= 0F) it.toString() else ""
    }
    setTextWithVisibilities(textView, text = resText, prefix = prefix, postfix = postfix)
}

@BindingAdapter(value = ["unixTime", "timeFormat", "prefix", "postfix"], requireAll = false)
fun setTimeFormatted(textView: TextView, unixTime: Long?, timeFormat: String?,
                     prefix: String? = null, postfix: String? = null) {

    if (unixTime == null || timeFormat == null) {
        textView.setVisible(false)
    } else {
        setTextWithVisibilities(textView,
                DateTimeUtil.formatDate(unixTime, timeFormat), prefix, postfix)
    }

}
