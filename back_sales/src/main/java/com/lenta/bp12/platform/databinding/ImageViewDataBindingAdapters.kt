package com.lenta.bp12.platform.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp12.R
import com.lenta.bp12.model.BlockType
import com.lenta.bp12.model.GoodType
import com.lenta.bp12.model.TaskStatus
import com.lenta.bp12.platform.extention.getDescriptionResId
import com.lenta.shared.utilities.databinding.dataBindingHelpHolder
import com.lenta.shared.utilities.extentions.setInvisible
import com.lenta.shared.utilities.extentions.setVisible

@BindingAdapter("taskStatusIcon")
fun setTaskStatusIcon(imageView: ImageView, taskStatus: TaskStatus) {
    imageView.apply {
        if (taskStatus == TaskStatus.COMMON) {
            setVisible(false)
        } else {
            setImageResource(when (taskStatus) {
                TaskStatus.STARTED -> R.drawable.ic_play_arrow_dark_24dp
                else -> R.drawable.ic_play_arrow_dark_24dp
            })
            setVisible()
        }
    }
}

@BindingAdapter("blockTypeIcon")
fun setBlockTypeIcon(imageView: ImageView, blockType: BlockType) {
    imageView.apply {
        if (blockType == BlockType.UNLOCK) {
            setVisible(false)
        } else {
            setImageResource(when (blockType) {
                BlockType.SELF_LOCK -> R.drawable.ic_lock_dark_24dp
                BlockType.LOCK -> R.drawable.ic_lock_open_dark_24dp
                else -> R.drawable.ic_play_arrow_dark_24dp
            })
            setVisible()
        }
    }
}

@BindingAdapter("goodTypeIcon")
fun setGoodTypeIcon(imageView: ImageView, goodType: GoodType?) {
    imageView.setImageResource(when (goodType) {
        GoodType.ALCOHOL -> R.drawable.ic_alcohol_32dp
        GoodType.EXCISE -> R.drawable.ic_marked_32dp
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
    })
}
