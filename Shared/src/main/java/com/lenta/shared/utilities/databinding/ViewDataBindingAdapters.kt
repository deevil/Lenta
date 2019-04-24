package com.lenta.shared.utilities.databinding

import android.view.View
import androidx.databinding.BindingAdapter
import com.lenta.shared.utilities.extentions.setInvisible
import com.lenta.shared.utilities.extentions.setVisible

@BindingAdapter("isGone")
fun setDisable(view: View, isGone: Boolean) {
    view.setVisible(!isGone)
}

@BindingAdapter("android:visibility")
fun setVisibility(view: View, visible: Boolean) {
    view.setVisible(visible)
}

@BindingAdapter("invisible")
fun setInvisible(view: View, invisible: Boolean) {
    view.setInvisible(invisible)
}