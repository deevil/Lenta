package com.lenta.bp7.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp7.R
import com.lenta.bp7.data.model.GoodStatus
import com.lenta.bp7.data.model.SegmentStatus
import com.lenta.bp7.data.model.ShelfStatus

@BindingAdapter("segmentStatusIcon")
fun setSegmentStatusIcon(imageView: ImageView, segmentStatus: SegmentStatus) {
    segmentStatus.let {
        val icon = when (it) {
            SegmentStatus.UNFINISHED -> R.drawable.ic_play_arrow_white_24dp
            SegmentStatus.PROCESSED -> R.drawable.ic_lock_white_24dp
            SegmentStatus.DELETED -> R.drawable.ic_delete_white_24dp
        }
        imageView.setImageResource(icon)
    }
}

@BindingAdapter("shelfStatusIcon")
fun setShelfStatusIcon(imageView: ImageView, shelfStatus: ShelfStatus) {
    shelfStatus.let {
        val icon = when (it) {
            ShelfStatus.UNFINISHED -> R.drawable.ic_add_white_24dp
            ShelfStatus.PROCESSED -> R.drawable.ic_lock_white_24dp
            ShelfStatus.DELETED -> R.drawable.ic_delete_white_24dp
        }
        imageView.setImageResource(icon)
    }
}

@BindingAdapter("goodStatusIcon")
fun setGoodStatusIcon(imageView: ImageView, goodStatus: GoodStatus) {
    goodStatus.let {
        val icon = when (it) {
            GoodStatus.CREATED -> R.drawable.ic_add_white_24dp
            GoodStatus.MISSING -> R.drawable.ic_close_white_24dp
            GoodStatus.PRESENT -> R.drawable.ic_done_white_24dp
        }
        imageView.setImageResource(icon)
    }
}