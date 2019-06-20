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
fun setAlcoIcon(imageView: ImageView, productType: ProductType?) {
    (when (productType) {
        ProductType.NonExciseAlcohol -> R.drawable.ic_no_excise_alco
        ProductType.ExciseAlcohol -> R.drawable.ic_excise_white_48dp
        else -> 0
    }).let { iconRes ->
        imageView.setImageResource(iconRes)
        (iconRes != 0).let { hasIcon ->
            imageView.setVisible(hasIcon)
            imageView.isFocusable = hasIcon
            if (hasIcon) {
                imageView.setOnClickListener {
                    dataBindingHelpHolder.coreNavigator.openAlertScreen(
                            message = imageView.context.getString(productType.getDescriptionResId()),
                            iconRes = iconRes
                    )
                }
                imageView.setBackgroundResource(imageView.context.selectableItemBackgroundResId())
            } else {
                imageView.setOnClickListener(null)
            }

        }

    }


}


