package com.lenta.bp16.platform.extention

import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.shared.utilities.orIfNull

fun OrderIngredientDataInfo.getItemName(): String {
    return name?.takeIf { it.isNotEmpty() }.orIfNull { matnr.orEmpty() }
}