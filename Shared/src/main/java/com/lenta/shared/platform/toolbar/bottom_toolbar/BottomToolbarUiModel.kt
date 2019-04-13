package com.lenta.shared.platform.toolbar.bottom_toolbar

import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.R
import com.lenta.shared.utilities.extentions.setTextViewDrawableColor

class BottomToolbarUiModel {
    val visibility: MutableLiveData<Boolean> = MutableLiveData()
    val uiModelButton1: ButtonUiModel = ButtonUiModel()
    val uiModelButton2: ButtonUiModel = ButtonUiModel()
    val uiModelButton3: ButtonUiModel = ButtonUiModel()
    val uiModelButton4: ButtonUiModel = ButtonUiModel()
    val uiModelButton5: ButtonUiModel = ButtonUiModel()
}

data class ButtonUiModel(
        val buttonDecorationInfo: MutableLiveData<ButtonDecorationInfo?> = MutableLiveData(),
        val visible: MutableLiveData<Boolean> = MutableLiveData(),
        val enabled: MutableLiveData<Boolean> = MutableLiveData()
)

data class ButtonDecorationInfo(
        @DrawableRes val iconRes: Int,
        @StringRes val titleRes: Int
)

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


