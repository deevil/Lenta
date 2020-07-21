package com.lenta.bp16.platform.extention

import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.shared.models.core.toUom
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.orIfNull

fun OrderIngredientDataInfo.getItemName(): String {
    return name.takeIf { it.isNotEmpty() }.orIfNull { matnr }
}

fun OrderIngredientDataInfo.getDoneCount(): String {
    return buildString {
        append(done_qnt.dropZeros())
        append(" ")
        append(buom.toUom().name)
    }
}

fun OrderIngredientDataInfo.getPlanCount(): String {
    return buildString {
        append(plan_qnt.dropZeros())
        append(" ")
        append(buom.toUom().name)
    }
}