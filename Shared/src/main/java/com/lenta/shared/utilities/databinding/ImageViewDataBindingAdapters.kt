package com.lenta.shared.utilities.databinding

import android.os.Build
import android.view.View.*
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
            imageView.isFocusable = false
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


@BindingAdapter("isStrictList")
fun setStrictListIcon(imageView: ImageView, isStrictList: Boolean?) {
    (when (isStrictList) {
        true -> R.drawable.ic_strict_list_32dp
        false -> R.drawable.ic_not_strict_list_48dp
        else -> 0
    }).let { iconRes ->
        imageView.setImageResource(iconRes)
        (iconRes != 0).let { hasIcon ->
            imageView.setVisible(hasIcon)
            //(BD) Договорились не давать возможность фокусировки на пиктограммах
            imageView.isFocusable = false
            if (hasIcon) {
                imageView.setOnClickListener {
                    dataBindingHelpHolder.coreNavigator.openAlertScreen(
                            message = imageView.context.getString(if (isStrictList == true) R.string.pictogram_strict_list else R.string.pictogram_not_strict_list),
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

@BindingAdapter("focusable")
fun setAlcoIcon(imageView: ImageView, focusable: Boolean?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        imageView.focusable = if (focusable == true) FOCUSABLE_AUTO else NOT_FOCUSABLE
    }
}

