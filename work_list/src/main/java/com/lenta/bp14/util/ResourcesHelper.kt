package com.lenta.bp14.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp14.R
import com.lenta.bp14.data.model.GoodStatus
import com.lenta.bp14.data.model.PriceTagStatus
import com.lenta.bp14.data.model.TaskStatus


@BindingAdapter("taskStatusIcon")
fun setTaskStatusIcon(imageView: ImageView, taskStatus: TaskStatus) {
    taskStatus.let {
        when (it) {
            TaskStatus.STARTED -> imageView.setImageResource(R.drawable.ic_play_arrow_white_24dp)
            TaskStatus.SELF_BLOCK -> imageView.setImageResource(R.drawable.ic_processed_status_white_24dp)
            TaskStatus.BLOCK -> imageView.setImageResource(R.drawable.ic_lock_white_24dp)
        }
    }
}

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