package com.lenta.bp12.platform.extention

import com.lenta.bp12.R
import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.GoodType
import com.lenta.bp12.request.GoodInfoResult
import com.lenta.shared.utilities.extentions.isSapTrue

fun GoodType.getDescriptionResId(): Int {
    return when (this) {
        GoodType.COMMON -> R.string.common_product
        GoodType.ALCOHOL -> R.string.alcohol
        GoodType.EXCISE -> R.string.excise_alcohol
    }
}

fun GoodInfoResult.getControlType(): ControlType {
    val isVet = this.materialInfo.isVet.isSapTrue()
    val isAlcohol = this.materialInfo.isAlcohol.isSapTrue()

    return when {
        !isAlcohol && !isVet -> ControlType.COMMON
        isAlcohol && !isVet -> ControlType.ALCOHOL
        else -> ControlType.UNKNOWN
    }
}