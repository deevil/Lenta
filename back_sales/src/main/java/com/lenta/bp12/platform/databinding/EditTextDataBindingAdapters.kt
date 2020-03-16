package com.lenta.bp12.platform.databinding

import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.lenta.bp12.R
import com.lenta.bp12.model.QuantityType

@BindingAdapter("quantityTypeIcon")
fun setQuantityTypeIcon(editText: EditText, quantityType: QuantityType) {
    val pieceIcon = ContextCompat.getDrawable(editText.context, R.drawable.ic_bei_12dp)
    val boxIcon = ContextCompat.getDrawable(editText.context, R.drawable.ic_eiz_12dp)

    val selectedIcon = when (quantityType) {
        QuantityType.CONSIGNMENT -> boxIcon
        QuantityType.MARK -> pieceIcon
        else -> pieceIcon
    }

    editText.setCompoundDrawables(null, null, selectedIcon, null)
}
