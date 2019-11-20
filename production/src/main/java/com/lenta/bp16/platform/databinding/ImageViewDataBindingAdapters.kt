package com.lenta.bp16.platform.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp16.R
import com.lenta.bp16.model.TaskType

@BindingAdapter("taskTypeIcon")
fun setTaskTypeIcon(imageView: ImageView, taskType: TaskType) {
    when (taskType) {
        TaskType.PACKING -> imageView.setImageResource(R.drawable.ic_recount_task)
        TaskType.DEFROZE -> imageView.setImageResource(R.drawable.ic_play_arrow_dark_24dp)
        TaskType.LOCK -> imageView.setImageResource(R.drawable.ic_lock_dark_24dp)
        TaskType.UNLOCK -> imageView.setImageResource(R.drawable.ic_lock_open_dark_24dp)
    }
}