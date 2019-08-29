package com.lenta.bp14.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp14.R
import com.lenta.bp14.data.model.GoodStatus
import com.lenta.bp14.data.model.PriceTagStatus


@BindingAdapter("goodStatusIcon")
fun setGoodStatusIcon(imageView: ImageView, goodStatus: GoodStatus) {
    goodStatus.let {
        when (it) {
            GoodStatus.PRESENT -> imageView.setImageResource(R.drawable.ic_done_white_24dp)
            GoodStatus.MISSING_RIGHT -> imageView.setImageResource(R.drawable.ic_error_outline_white_24dp)
            GoodStatus.MISSING_WRONG -> imageView.setImageResource(R.drawable.ic_close_white_24dp)
        }
    }
}

@BindingAdapter("priceTagStatusIcon")
fun setPriceTagStatusIcon(imageView: ImageView, priceTagStatus: PriceTagStatus) {
    priceTagStatus.let {
        when (it) {
            PriceTagStatus.NO_PRICE_TAG -> imageView.setImageResource(R.drawable.ic_missing_wrong_status_white_24dp)
            PriceTagStatus.WITH_ERROR -> imageView.setImageResource(R.drawable.ic_error_outline_white_24dp)
            PriceTagStatus.PRINTED -> imageView.setImageResource(R.drawable.ic_print_white_24dp)
        }
    }
}