package com.lenta.bp12.platform.databinding

import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.lenta.bp12.R

@BindingAdapter("quantityTypeIcon")
fun setQuantityTypeIcon(editText: EditText, differentUnits: Boolean) {
    val icon = if (differentUnits) {
        ContextCompat.getDrawable(editText.context, R.drawable.ic_eiz_12dp)
    } else {
        ContextCompat.getDrawable(editText.context, R.drawable.ic_bei_12dp)
    }

    editText.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
}
