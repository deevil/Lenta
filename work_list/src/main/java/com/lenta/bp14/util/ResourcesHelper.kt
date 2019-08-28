package com.lenta.bp14.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp14.R
import com.lenta.bp14.data.model.GoodStatus
import com.lenta.bp14.data.model.PriceTagStatus
import com.lenta.shared.utilities.extentions.setInvisible
import com.lenta.shared.utilities.extentions.setVisible


@BindingAdapter("goodStatusIcon")
fun setGoodStatusIcon(imageView: ImageView, goodStatus: GoodStatus) {
    goodStatus.let {
        imageView.setVisible()

        when (it) {
            GoodStatus.MISSING -> imageView.setImageResource(R.drawable.ic_missing_wrong_status_white_24dp)
            GoodStatus.ERROR -> imageView.setImageResource(R.drawable.ic_error_outline_white_24dp)
            else -> imageView.setInvisible()
        }
    }
}

@BindingAdapter("priceTagStatusIcon")
fun setPriceTagStatusIcon(imageView: ImageView, priceTagStatus: PriceTagStatus) {
    priceTagStatus.let {
        imageView.setVisible()

        when (it) {
            PriceTagStatus.MISSING -> imageView.setImageResource(R.drawable.ic_missing_wrong_status_white_24dp)
            PriceTagStatus.PRINTED -> imageView.setImageResource(R.drawable.ic_print_white_24dp)
            else -> imageView.setInvisible()
        }
    }
}