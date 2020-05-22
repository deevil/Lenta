package com.lenta.bp14.platform.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp14.R
import com.lenta.bp14.features.task_list.TaskBlockingStatus
import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.data.getDescriptionResId
import com.lenta.shared.utilities.databinding.dataBindingHelpHolder
import com.lenta.shared.utilities.extentions.setInvisible
import com.lenta.shared.utilities.extentions.setVisible

@BindingAdapter("taskStatusIcon")
fun setTaskStatusIcon(imageView: ImageView, taskBlockingStatus: TaskBlockingStatus) {
    taskBlockingStatus.let {
        when (it) {
            TaskBlockingStatus.NOT_BLOCKED -> imageView.setImageResource(R.drawable.ic_play_arrow_white_24dp)
            TaskBlockingStatus.SELF_BLOCK -> imageView.setImageResource(R.drawable.ic_lock_open_dark_24dp)
            TaskBlockingStatus.BLOCK -> imageView.setImageResource(R.drawable.ic_processed_status_dark_24dp)
        }
        imageView.setVisible(it != TaskBlockingStatus.NOT_BLOCKED)
    }
}

@BindingAdapter("isValid")
fun setPriceTagStatusIcon(imageView: ImageView, isValid: Boolean?) {
    when (isValid) {
        true -> imageView.setImageResource(R.drawable.ic_done_white_24dp)
        false -> imageView.setImageResource(R.drawable.ic_close_white_24dp)
    }
    imageView.setVisible(isValid != null)
}

@BindingAdapter("isValidPrice")
fun setValidPriceStatusIcon(imageView: ImageView, isValidPrice: Boolean?) {
    when (isValidPrice) {
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
    imageView.setImageResource(when (goodType) {
        GoodType.ALCOHOL -> R.drawable.ic_no_excise_alcohol_white_32dp
        GoodType.EXCISE -> R.drawable.ic_excise_alcohol_white_32dp
        GoodType.MARKED -> R.drawable.ic_marked_white_32dp
        else -> R.drawable.ic_kandy_32dp
    }.also { iconRes ->

        imageView.setInvisible(goodType == null)

        goodType?.let {
            imageView.setOnClickListener {
                dataBindingHelpHolder.coreNavigator.openAlertScreen(
                        message = imageView.context.getString(goodType.getDescriptionResId()),
                        iconRes = iconRes
                )
            }
        }
    }
    )
}