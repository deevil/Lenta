package com.lenta.bp12.platform.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp12.R
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.platform.extention.getDescriptionResId
import com.lenta.shared.utilities.databinding.dataBindingHelpHolder
import com.lenta.shared.utilities.extentions.setInvisible

@BindingAdapter("goodKindIcon")
fun setGoodKindIcon(imageView: ImageView, goodKind: GoodKind?) {
    imageView.setImageResource(when (goodKind) {
        GoodKind.ALCOHOL -> R.drawable.ic_no_excise_alco_32dp
        GoodKind.EXCISE -> R.drawable.ic_excise_alcohol_white_32dp
        GoodKind.MARK -> R.drawable.ic_marked_white_32dp
        else -> R.drawable.ic_kandy_white_32dp
    }.also { iconRes ->
        imageView.setInvisible(goodKind == null)

        goodKind?.let {
            imageView.setOnClickListener {
                dataBindingHelpHolder.coreNavigator.openAlertScreen(
                        message = imageView.context.getString(goodKind.getDescriptionResId()),
                        iconRes = iconRes
                )
            }
        }
    })
}
