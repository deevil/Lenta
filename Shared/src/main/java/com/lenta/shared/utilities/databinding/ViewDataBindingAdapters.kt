package com.lenta.shared.utilities.databinding

import android.view.View
import androidx.databinding.BindingAdapter
import com.lenta.shared.utilities.extentions.setInvisible
import com.lenta.shared.utilities.extentions.setVisible

@BindingAdapter("isGone")
fun setIsGone(view: View, isGone: Boolean?) {
    view.setVisible(!(isGone ?: false))
}

@BindingAdapter("android:visibility")
fun setVisibility(view: View, visible: Boolean?) {
    view.setVisible(visible ?: false)
}

@BindingAdapter("invisible")
fun setInvisible(view: View, invisible: Boolean?) {
    view.setInvisible(invisible ?: false)
}