package com.lenta.bp9.platform.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp9.R
import com.lenta.bp9.features.task_list.TaskPostponedStatus
import com.lenta.bp9.model.task.TaskLockStatus
import com.lenta.shared.utilities.extentions.setVisible
import com.lenta.shared.utilities.extentions.setVisibleGone

@BindingAdapter("postponedTaskStatusIcon")
fun setPostponedStatusIcon(imageView: ImageView, statusType: TaskPostponedStatus?) {
    statusType?.getIconRes()?.let {
        imageView.setVisible()
        imageView.setImageResource(it)
        return
    }
    imageView.setVisibleGone()
}

@BindingAdapter("lockTaskStatusIcon")
fun setLockStatusIcon(imageView: ImageView, statusType: TaskLockStatus?) {
    statusType?.getIconRes()?.let {
        imageView.setVisible()
        imageView.setImageResource(it)
        return
    }
    imageView.setVisibleGone()
}

fun TaskPostponedStatus.getIconRes(): Int? {
    return when (this) {
        TaskPostponedStatus.PauseSign -> R.drawable.ic_checkbox_green_32dp //TODO: Proper icon
        TaskPostponedStatus.PlaySign -> R.drawable.ic_play_arrow_white_24dp
        else -> null
    }
}

fun TaskLockStatus.getIconRes(): Int? {
    return when (this) {
        TaskLockStatus.LockedByMe -> R.drawable.ic_checkbox_green_32dp //TODO: Proper icon
        TaskLockStatus.LockedByOthers -> R.drawable.ic_lock_white_24dp
        else -> null
    }
}