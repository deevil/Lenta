package com.lenta.bp10.platform.databinding.adapters

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.lenta.shared.R
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.isNormal

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

}