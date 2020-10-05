package com.lenta.shared.utilities.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.shared.R
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.getDescriptionResId
import com.lenta.shared.utilities.BlockType
import com.lenta.shared.utilities.extentions.selectableItemBackgroundResId
import com.lenta.shared.utilities.extentions.setVisible

@BindingAdapter("android:src")
fun setImageSrc(imageView: ImageView, imageRes: Int) {
    imageView.setImageResource(imageRes)
}

@BindingAdapter("blockTypeIcon")
fun setBlockTypeIcon(imageView: ImageView, blockType: BlockType) {
    imageView.apply {
        if (blockType == BlockType.UNLOCK) {
            setVisible(false)
        } else {
            setImageResource(when (blockType) {
                BlockType.SELF_LOCK -> R.drawable.ic_self_lock_status_gray_24dp
                BlockType.LOCK -> R.drawable.ic_lock_status_gray_24dp
                else -> R.drawable.ic_play_arrow_gray_24dp
            })
            setVisible()
        }
    }
}

@BindingAdapter("goodTypeIcon")
fun setGoodTypeIcon(imageView: ImageView, productType: ProductType?) {
    (when (productType) {
        ProductType.NonExciseAlcohol -> R.drawable.ic_no_excise_alcohol_white_32dp
        ProductType.ExciseAlcohol -> R.drawable.ic_excise_alcohol_white_32dp
        ProductType.Marked -> R.drawable.ic_marked_white_32dp
        ProductType.ZBatch -> R.drawable.ic_zbatch_white_32dp
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
        true -> R.drawable.ic_strict_list_white_32dp
        false -> R.drawable.ic_not_strict_list_white_32dp
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
                            message = imageView.context.getString(if (isStrictList == true) R.string.strict_list else R.string.not_strict_list),
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