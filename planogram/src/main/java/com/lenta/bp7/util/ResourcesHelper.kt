package com.lenta.bp7.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp7.R
import com.lenta.bp7.data.model.SegmentStatus

@BindingAdapter("segmentStatusIcon")
fun setSegmentStatusIcon(imageView: ImageView, segmentStatus: SegmentStatus) {
    segmentStatus.getIconRes().let {
        imageView.setImageResource(it)
    }
}

fun SegmentStatus.getIconRes(): Int {
    return when (this) {
        SegmentStatus.STARTED -> R.drawable.ic_add_white_24dp
        SegmentStatus.UNFINISHED -> R.drawable.ic_play_arrow_white_24dp
        SegmentStatus.PROCESSED -> R.drawable.ic_lock_white_24dp
        SegmentStatus.DELETED -> R.drawable.ic_delete_white_24dp
    }
}