package com.lenta.bp16.platform.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp16.R
import com.lenta.bp16.model.IngredientStatus
import com.lenta.bp16.model.TaskStatus
import com.lenta.shared.utilities.extentions.setVisible

@BindingAdapter("taskStatusIcon")
fun setTaskStatusIcon(imageView: ImageView, taskStatus: TaskStatus) {
    imageView.apply {
        if (taskStatus == TaskStatus.COMMON) {
            setVisible(false)
        } else {
            setImageResource(when (taskStatus) {
                TaskStatus.LOCK -> R.drawable.ic_lock_status_gray_24dp
                TaskStatus.SELF_LOCK -> R.drawable.ic_self_lock_status_gray_24dp
                else -> R.drawable.ic_play_arrow_gray_24dp
            })
            setVisible()
        }
    }
}

@BindingAdapter("ingredientStatusIcon")
fun setIngredientStatusIcon(imageView: ImageView, ingredientStatus: IngredientStatus) {
    imageView.apply {
        if (ingredientStatus == IngredientStatus.COMMON) {
            setVisible(false)
        } else {
            setImageResource(when (ingredientStatus) {
                IngredientStatus.LOCK -> R.drawable.ic_lock_status_gray_24dp
                IngredientStatus.SELF_LOCK -> R.drawable.ic_self_lock_status_gray_24dp
                else -> R.drawable.ic_play_arrow_gray_24dp
            })
            setVisible()
        }
    }
}

@BindingAdapter("taskPackIcon")
fun setTaskPackIcon(imageView: ImageView, isPack: Boolean) {
    imageView.apply {
        setVisible(isPack)
    }
}