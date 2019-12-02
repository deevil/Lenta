package com.lenta.bp16.platform.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp16.R
import com.lenta.bp16.model.TaskStatus
import com.lenta.shared.utilities.extentions.setInvisible

@BindingAdapter("taskTypeIcon")
fun setTaskTypeIcon(imageView: ImageView, taskStatus: TaskStatus) {
    imageView.apply {
        if (taskStatus == TaskStatus.COMMON) {
            setInvisible()
        } else {
            setImageResource(when (taskStatus) {
                TaskStatus.PACKING -> R.drawable.ic_recount_task
                TaskStatus.LOCK -> R.drawable.ic_lock_dark_24dp
                TaskStatus.SELF_LOCK -> R.drawable.ic_lock_open_dark_24dp
                else -> R.drawable.ic_play_arrow_dark_24dp
            })
        }
    }
}