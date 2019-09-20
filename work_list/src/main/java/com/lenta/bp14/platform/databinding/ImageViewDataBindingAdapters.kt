package com.lenta.bp14.platform.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp14.R
import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.data.pojo.PriceTagStatus
import com.lenta.bp14.models.data.pojo.PrintStatus
import com.lenta.bp14.models.data.pojo.TaskStatus
import com.lenta.shared.utilities.extentions.setVisible

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

@BindingAdapter("isValid")
fun setPriceTagStatusIcon(imageView: ImageView, isValid: Boolean?) {
    when (isValid) {
        true -> imageView.setImageResource(R.drawable.ic_done_white_24dp)
        false -> imageView.setImageResource(R.drawable.ic_close_white_24dp)
        null -> imageView.setImageResource(R.drawable.ic_missing_dark_24dp)
    }
}

@BindingAdapter("isPrinted")
fun setPrintStatusIcon(imageView: ImageView, isPrinted: Boolean?) {
    when (isPrinted) {
        true -> {
            imageView.setVisible(true)
            imageView.setImageResource(R.drawable.ic_print_dark_24dp)
        }
        false -> {
            imageView.setVisible(true)
            imageView.setImageResource(R.drawable.ic_print_no_dark_24dp)
        }
        null -> imageView.setVisible(false)
    }
}

@BindingAdapter("goodTypeIcon")
fun setGoodTypeIcon(imageView: ImageView, goodType: GoodType?) {
    when (goodType) {
        GoodType.ALCOHOL -> imageView.setImageResource(R.drawable.ic_alco_white_48dp)
        GoodType.MARKED -> imageView.setImageResource(R.drawable.ic_marked_white_48dp)
        else -> imageView.setImageResource(R.drawable.ic_kandy_48dp)
    }
}