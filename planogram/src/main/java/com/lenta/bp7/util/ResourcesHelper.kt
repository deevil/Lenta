package com.lenta.bp7.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp7.R
import com.lenta.bp7.data.model.GoodStatus
import com.lenta.bp7.data.model.SegmentStatus
import com.lenta.bp7.data.model.ShelfStatus
import com.lenta.shared.utilities.extentions.setInvisible
import com.lenta.shared.utilities.extentions.setVisible

@BindingAdapter("segmentStatusIcon")
fun setSegmentStatusIcon(imageView: ImageView, segmentStatus: SegmentStatus) {
    segmentStatus.let {
        val icon = when (it) {
            SegmentStatus.UNFINISHED -> R.drawable.ic_created_status_white_24dp
            SegmentStatus.PROCESSED -> R.drawable.ic_lock_status_gray_24dp
            SegmentStatus.DELETED -> R.drawable.ic_deleted_status_white_24dp
        }
        imageView.setImageResource(icon)
    }
}

@BindingAdapter("shelfStatusIcon")
fun setShelfStatusIcon(imageView: ImageView, shelfStatus: ShelfStatus) {
    shelfStatus.let {
        val icon = when (it) {
            ShelfStatus.UNFINISHED -> R.drawable.ic_created_status_white_24dp
            ShelfStatus.PROCESSED -> R.drawable.ic_lock_status_gray_24dp
            ShelfStatus.DELETED -> R.drawable.ic_deleted_status_white_24dp
        }
        imageView.setImageResource(icon)
    }
}

@BindingAdapter("goodStatusIcon")
fun setGoodStatusIcon(imageView: ImageView, goodStatus: GoodStatus) {
    goodStatus.let {
        imageView.setVisible()

        when (it) {
            GoodStatus.MISSING_WRONG -> imageView.setImageResource(R.drawable.ic_missing_wrong_status_white_24dp)
            GoodStatus.MISSING_RIGHT -> imageView.setImageResource(R.drawable.ic_missing_rigth_status_white_24dp)
            else -> imageView.setInvisible()
        }
    }
}