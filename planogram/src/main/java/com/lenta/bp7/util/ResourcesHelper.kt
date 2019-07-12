package com.lenta.bp7.util

import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.lenta.bp7.R
import com.lenta.bp7.data.model.SegmentStatus

@BindingAdapter("segmentStatusIcon")
fun setSegmentStatusIcon(imageView: ImageView, segmentStatus: SegmentStatus) {
    segmentStatus.let {
        imageView.setImageResource(it.getIconRes())
        imageView.setColorFilter(ResourcesCompat.getColor(imageView.context.resources, it.getColorRes(), null))
    }
}

@BindingAdapter("segmentTextColor")
fun setSegmentTextColor(textView: TextView, segmentStatus: SegmentStatus) {
    segmentStatus.let {
        textView.setTextColor(ResourcesCompat.getColor(textView.context.resources, it.getColorRes(), null))
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

fun SegmentStatus.getColorRes(): Int {
    return when (this) {
        SegmentStatus.STARTED -> R.color.segment_other_status
        SegmentStatus.UNFINISHED -> R.color.segment_deleted_status
        SegmentStatus.PROCESSED -> R.color.segment_other_status
        SegmentStatus.DELETED -> R.color.segment_pocessed_status
    }
}