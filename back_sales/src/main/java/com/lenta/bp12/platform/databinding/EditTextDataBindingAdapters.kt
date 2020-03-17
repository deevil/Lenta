package com.lenta.bp12.platform.databinding

import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.lenta.bp12.R
import com.lenta.shared.models.core.Uom

@BindingAdapter("quantityTypeIcon")
fun setQuantityTypeIcon(editText: EditText, orderUnits: Uom) {
    val icon = when (orderUnits) {
        Uom.KAR -> ContextCompat.getDrawable(editText.context, R.drawable.ic_eiz_12dp)
        else -> ContextCompat.getDrawable(editText.context, R.drawable.ic_bei_12dp)
    }

    editText.setCompoundDrawables(null, null, icon, null)
}
