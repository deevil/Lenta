package com.lenta.shared.utilities.extentions

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.core.content.ContextCompat

fun TextView.setTextViewDrawableColor(color: Int) {
    for (drawable: Drawable? in this.compoundDrawables) {
        drawable?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this.context, color), PorterDuff.Mode.SRC_IN)
    }
}