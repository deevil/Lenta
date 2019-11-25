package com.lenta.bp16.platform.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp16.R
import com.lenta.bp16.model.TaskType
import com.lenta.shared.utilities.extentions.setInvisible

@BindingAdapter("taskTypeIcon")
fun setTaskTypeIcon(imageView: ImageView, taskType: TaskType) {
    imageView.apply {
        if (taskType == TaskType.COMMON) {
            setInvisible()
        } else {
            setImageResource(when (taskType) {
                TaskType.PACKING -> R.drawable.ic_recount_task
                TaskType.LOCK -> R.drawable.ic_lock_dark_24dp
                TaskType.SELF_LOCK -> R.drawable.ic_lock_open_dark_24dp
                else -> R.drawable.ic_play_arrow_dark_24dp
            })
        }
    }
}