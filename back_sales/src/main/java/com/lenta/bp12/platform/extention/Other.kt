package com.lenta.bp12.platform.extention

import com.lenta.bp12.R
import com.lenta.bp12.model.GoodType

fun GoodType.getDescriptionResId(): Int {
    return when (this) {
        GoodType.COMMON -> R.string.common_product
        GoodType.ALCOHOL -> R.string.alcohol
        GoodType.EXCISE -> R.string.excise_alcohol
    }
}