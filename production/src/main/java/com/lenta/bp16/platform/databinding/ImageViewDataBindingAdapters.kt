package com.lenta.bp16.platform.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp16.R
import com.lenta.bp16.model.GoodTypeIcon
import com.lenta.bp16.model.IngredientStatusBlock
import com.lenta.bp16.model.IngredientStatusWork
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

@BindingAdapter("ingredientStatusBlockIcon")
fun setIngredientStatusBlockIcon(imageView: ImageView, ingredientStatusBlock: IngredientStatusBlock) {
    imageView.apply {
        if (ingredientStatusBlock == IngredientStatusBlock.COMMON) {
            setVisible(false)
        } else {
            setImageResource(when (ingredientStatusBlock) {
                IngredientStatusBlock.LOCK -> R.drawable.ic_lock_status_gray_24dp
                IngredientStatusBlock.SELF_LOCK -> R.drawable.ic_self_lock_status_gray_24dp
                else -> 0
            })
            setVisible()
        }
    }
}

@BindingAdapter("ingredientStatusWorkIcon")
fun setIngredientStatusWorkIcon(imageView: ImageView, ingredientStatusWork: IngredientStatusWork) {
    imageView.apply {
        if (ingredientStatusWork == IngredientStatusWork.COMMON) {
            setVisible(false)
        } else {
            setImageResource(when (ingredientStatusWork) {
                IngredientStatusWork.IS_PLAY ->  R.drawable.ic_play_arrow_gray_24dp
                IngredientStatusWork.IS_DONE ->  R.drawable.ic_done_white_24dp
                else -> 0
            })
            setVisible()
        }
    }
}

@BindingAdapter("goodTypeIcon")
fun setGoodTypeIcon(imageView: ImageView, goodType: GoodTypeIcon) {
    imageView.apply {
            setImageResource(when (goodType) {
                GoodTypeIcon.PLAN ->  R.drawable.ic_plan_attribute_white_32dp
                GoodTypeIcon.FACT ->  R.drawable.ic_fact_attribute_white_32dp
                GoodTypeIcon.VET -> R.drawable.ic_mercury_white_32dp
            })
            setVisible()
    }
}

@BindingAdapter("taskPackIcon")
fun setTaskPackIcon(imageView: ImageView, isPack: Boolean) {
    imageView.apply {
        setVisible(isPack)
    }
}