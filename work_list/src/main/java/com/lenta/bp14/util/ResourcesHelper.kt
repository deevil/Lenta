package com.lenta.bp14.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp14.R
import com.lenta.bp14.data.model.PriceTagStatus
import com.lenta.bp14.data.model.PrintStatus
import com.lenta.bp14.data.model.TaskStatus

@BindingAdapter("taskStatusIcon")
fun setTaskStatusIcon(imageView: ImageView, taskStatus: TaskStatus) {
    taskStatus.let {
        when (it) {
            TaskStatus.STARTED -> imageView.setImageResource(R.drawable.ic_play_arrow_dark_24dp)
            TaskStatus.SELF_BLOCK -> imageView.setImageResource(R.drawable.ic_lock_open_dark_24dp)
            TaskStatus.BLOCK -> imageView.setImageResource(R.drawable.ic_processed_status_dark_24dp)
        }
    }
}

@BindingAdapter("priceTagStatusIcon")
fun setPriceTagStatusIcon(imageView: ImageView, priceTagStatus: PriceTagStatus) {
    priceTagStatus.let {
        when (it) {
            PriceTagStatus.CORRECT -> imageView.setImageResource(R.drawable.ic_done_white_24dp)
            PriceTagStatus.WITH_ERROR -> imageView.setImageResource(R.drawable.ic_close_white_24dp)
            PriceTagStatus.MISSING -> imageView.setImageResource(R.drawable.ic_missing_dark_24dp)
        }
    }
}

@BindingAdapter("printStatusIcon")
fun setPrintStatusIcon(imageView: ImageView, printStatus: PrintStatus) {
    printStatus.let {
        when (it) {
            PrintStatus.NOT_PRINTED -> imageView.setImageResource(R.drawable.ic_print_no_dark_24dp)
            PrintStatus.PRINTED -> imageView.setImageResource(R.drawable.ic_print_dark_24dp)
        }
    }
}