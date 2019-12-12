package com.lenta.shared.utilities.databinding

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.lenta.shared.R
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.isNormal
import com.lenta.shared.platform.battery_state.getIconForStatus
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.setTextViewDrawableColor
import com.lenta.shared.utilities.extentions.setVisible
import com.lenta.shared.utilities.extentions.selectableItemBackgroundResId
import com.lenta.shared.utilities.extentions.setVisibleGone


@BindingAdapter(value = ["buttonDecorationInfo", "enabled"], requireAll = false)
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
fun setBatteryLevel(textView: TextView, batteryLevel: Int?, isCharging: Boolean?) {
    intWithVisibilities(textView, batteryLevel, postfix = "%")
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

@BindingAdapter(value = ["textColorCustom"])
fun setTextColor(textView: TextView, textColorCustom: Int?) {
    textColorCustom?.let {
        textView.setTextColor(it)
    }
}

@BindingAdapter(value = ["setTextWithVisibilities", "prefix", "postfix"], requireAll = false)
fun setTextWithVisibilities(textView: TextView, text: String?, prefix: String?, postfix: String?) {
    textView.setVisible(!text.isNullOrEmpty())
    val resText = "${prefix ?: ""}${text ?: ""}${postfix ?: ""}"
    textView.text = resText
}

@BindingAdapter(value = ["intWithVisibilities", "prefix", "postfix"], requireAll = false)
fun intWithVisibilities(textView: TextView, counter: Int?, prefix: String? = null, postfix: String? = null) {
    var resText = ""
    counter?.let {
        resText = if (it > -1) it.toString() else ""
    }
    setTextWithVisibilities(textView, text = resText, prefix = prefix, postfix = postfix)
}

@BindingAdapter(value = ["floatWithVisibilities", "prefix", "postfix"], requireAll = false)
fun floatWithVisibilities(textView: TextView, value: Float?, prefix: String? = null, postfix: String? = null) {
    var resText = ""
    value?.let {
        resText = if (it >= 0F) it.toString() else ""
    }
    setTextWithVisibilities(textView, text = resText, prefix = prefix, postfix = postfix)
}

@BindingAdapter(value = ["price"])
fun price(textView: TextView, price: Double?) {
    textView.text = if (
            price != null
            && price > 0F
    )
        "${String.format("%.2f", price)} р."
    else ""
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

@BindingAdapter(value = ["matrixType"])
fun setMatrixType(textView: TextView, matrixType: MatrixType?) {
    val text = when (matrixType) {
        MatrixType.Active -> "A"
        MatrixType.Passive -> "P"
        MatrixType.Deleted -> "D"
        else -> "N"
    }
    textView.text = text
    (matrixType.isNormal()).let { normalType ->
        textView.setTextColor(ContextCompat.getColor(textView.context,
                if (normalType) R.color.colorNumSectionTxt else R.color.color_text_pink
        ))
        textView.setBackgroundResource(if (normalType) R.drawable.bg_white_circle else R.drawable.bg_pink_circle)
    }

    matrixType?.let {
        textView.setOnClickListener {
            dataBindingHelpHolder.coreNavigator.openMatrixInfoScreen(matrixType)
        }
    }


}

@BindingAdapter(value = ["section"])
fun setSectionInfo(textView: TextView, section: String?) {
    section?.let {
        textView.setVisible()
        textView.text = section
        textView.setBackgroundResource(R.drawable.bg_white_circle)
        textView.setOnClickListener {
            dataBindingHelpHolder.coreNavigator.openSectionInfoScreen(section)
        }
        return
    }
    textView.setVisibleGone()

}

@BindingAdapter(value = ["zoom"], requireAll = false)
fun setTextWithVisibilities(textView: TextView, @Suppress("UNUSED_PARAMETER") screenNavigatorForZoom: Boolean) {
    textView.setOnClickListener {
        dataBindingHelpHolder.coreNavigator.openAlertScreen(message = textView.text.toString(), onlyIfFirstAlert = true)
    }
    textView.setBackgroundResource(textView.context.selectableItemBackgroundResId())
}