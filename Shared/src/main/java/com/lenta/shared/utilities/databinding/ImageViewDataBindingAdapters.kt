package com.lenta.shared.utilities.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.shared.R
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.getDescriptionResId
import com.lenta.shared.utilities.extentions.selectableItemBackgroundResId
import com.lenta.shared.utilities.extentions.setVisible

@BindingAdapter("android:src")
fun setImageSrc(imageView: ImageView, imageRes: Int) {
    imageView.setImageResource(imageRes)
}

@BindingAdapter("alcoIcon")
fun setImageSrc(imageView: ImageView, productType: ProductType?) {
    (when (productType) {
        ProductType.NonExciseAlcohol -> R.drawable.ic_no_excise_alco
        else -> 0
    }).let {
        imageView.setImageResource(it)
        imageView.setVisible(it != 0)
        if (it == 0) {
            imageView.setOnClickListener(null)
        } else {
            imageView.setOnClickListener {
                dataBindingHelpHolder.coreNavigator.openAlertScreen(message = imageView.context.getString(productType.getDescriptionResId()))
            }
        }
        imageView.setBackgroundResource(imageView.context.selectableItemBackgroundResId())
    }


}


