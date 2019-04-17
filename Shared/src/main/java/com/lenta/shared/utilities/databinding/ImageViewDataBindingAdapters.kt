package com.lenta.shared.utilities.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("android:src")
fun setImageSrc(imageView: ImageView, imageRes: Int) {
    imageView.setImageResource(imageRes)
}