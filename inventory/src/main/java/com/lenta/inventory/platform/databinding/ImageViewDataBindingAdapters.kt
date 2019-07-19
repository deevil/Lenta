package com.lenta.inventory.platform.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.inventory.features.task_list.StatusTask
import com.lenta.inventory.models.StorePlaceStatus
import com.lenta.shared.utilities.extentions.setVisible
import com.lenta.shared.utilities.extentions.setVisibleGone


@BindingAdapter("statusTaskIcon")
fun setAlcoIcon(imageView: ImageView, statusType: StatusTask?) {
    statusType?.getIconRes()?.let {
        imageView.setVisible()
        imageView.setImageResource(it)
        return
    }
    imageView.setVisibleGone()
}

@BindingAdapter("StorePlaceStatusIcon")
fun setStorePlaceStatusIcon(imageView: ImageView, storePlaceStatus: StorePlaceStatus?) {
    storePlaceStatus?.getIconRes()?.let {
        imageView.setVisible()
        imageView.setImageResource(it)
        return
    }
    imageView.setVisibleGone()
}


fun StatusTask.getIconRes(): Int? {
    return when (this) {
        StatusTask.BlockedMe -> com.lenta.inventory.R.drawable.ic_lock_open_white_32
        StatusTask.BlockedNotMe -> com.lenta.inventory.R.drawable.ic_lock_close_white_32
        StatusTask.Parallels -> com.lenta.inventory.R.drawable.ic_people_white_32
        StatusTask.Processed -> com.lenta.inventory.R.drawable.ic_play_white_32
        else -> null
    }
}

fun StorePlaceStatus.getIconRes(): Int? {
    return when (this) {
        StorePlaceStatus.LockedByMe -> com.lenta.inventory.R.drawable.ic_lock_open_white_32
        StorePlaceStatus.LockedByOthers -> com.lenta.inventory.R.drawable.ic_lock_close_white_32
        StorePlaceStatus.Finished -> com.lenta.inventory.R.drawable.ic_checkbox_green_32dp
        StorePlaceStatus.Started -> com.lenta.inventory.R.drawable.ic_battery_20_red_16dp //TODO: proper image
        else -> null
    }
}