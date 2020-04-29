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
        StatusTask.BlockedMe -> com.lenta.inventory.R.drawable.ic_self_lock_status_dark_24dp
        StatusTask.BlockedNotMe -> com.lenta.inventory.R.drawable.ic_lock_status_dark_24dp
        StatusTask.Parallels -> com.lenta.inventory.R.drawable.ic_group_dark_24dp
        StatusTask.Processed -> com.lenta.inventory.R.drawable.ic_play_arrow_dark_24dp
        else -> null
    }
}

fun StorePlaceStatus.getIconRes(): Int? {
    return when (this) {
        StorePlaceStatus.LockedByMe -> com.lenta.inventory.R.drawable.ic_self_lock_status_dark_24dp
        StorePlaceStatus.LockedByOthers -> com.lenta.inventory.R.drawable.ic_lock_status_dark_24dp
        StorePlaceStatus.Finished -> com.lenta.inventory.R.drawable.ic_checkbox_green_24dp
        StorePlaceStatus.Started -> com.lenta.inventory.R.drawable.ic_play_arrow_dark_24dp
        else -> null
    }
}