package com.lenta.bp7.util

import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.lenta.bp7.R
import com.lenta.bp7.data.model.GoodStatus
import com.lenta.bp7.data.model.SegmentStatus
import com.lenta.bp7.data.model.ShelfStatus

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
        SegmentStatus.UNFINISHED -> R.drawable.ic_play_arrow_white_24dp
        SegmentStatus.PROCESSED -> R.drawable.ic_lock_white_24dp
        SegmentStatus.DELETED -> R.drawable.ic_delete_white_24dp
    }
}

fun SegmentStatus.getColorRes(): Int {
    return when (this) {
        SegmentStatus.UNFINISHED -> R.color.status_unfinished
        SegmentStatus.PROCESSED -> R.color.status_other
        SegmentStatus.DELETED -> R.color.status_deleted
    }
}

@BindingAdapter("shelfStatusIcon")
fun setShelfStatusIcon(imageView: ImageView, shelfStatus: ShelfStatus) {
    shelfStatus.let {
        imageView.setImageResource(it.getIconRes())
        imageView.setColorFilter(ResourcesCompat.getColor(imageView.context.resources, it.getColorRes(), null))
    }
}

@BindingAdapter("shelfTextColor")
fun setShelfTextColor(textView: TextView, shelfStatus: ShelfStatus) {
    shelfStatus.let {
        textView.setTextColor(ResourcesCompat.getColor(textView.context.resources, it.getColorRes(), null))
    }
}

fun ShelfStatus.getIconRes(): Int {
    return when (this) {
        ShelfStatus.CREATED -> R.drawable.ic_add_white_24dp
        ShelfStatus.PROCESSED -> R.drawable.ic_lock_white_24dp
        ShelfStatus.DELETED -> R.drawable.ic_delete_white_24dp
    }
}

fun ShelfStatus.getColorRes(): Int {
    return when (this) {
        ShelfStatus.CREATED -> R.color.status_other
        ShelfStatus.PROCESSED -> R.color.status_other
        ShelfStatus.DELETED -> R.color.status_deleted
    }
}

@BindingAdapter("goodStatusIcon")
fun setGoodStatusIcon(imageView: ImageView, goodStatus: GoodStatus) {
    goodStatus.let {
        imageView.setImageResource(it.getIconRes())
        imageView.setColorFilter(ResourcesCompat.getColor(imageView.context.resources, it.getColorRes(), null))
    }
}

fun GoodStatus.getIconRes(): Int {
    return when (this) {
        GoodStatus.CREATED -> R.drawable.ic_add_white_24dp
        GoodStatus.MISSING -> R.drawable.ic_close_white_24dp
        GoodStatus.PRESENT -> R.drawable.ic_done_white_24dp
    }
}

fun GoodStatus.getColorRes(): Int {
    return when (this) {
        GoodStatus.CREATED -> R.color.status_other
        GoodStatus.MISSING -> R.color.status_missing
        GoodStatus.PRESENT -> R.color.status_present
    }
}