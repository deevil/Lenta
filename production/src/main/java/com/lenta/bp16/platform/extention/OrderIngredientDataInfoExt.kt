package com.lenta.bp16.platform.extention

import com.lenta.bp16.model.ingredients.ui.OrderIngredientDataInfoUI
import com.lenta.shared.utilities.orIfNull

fun OrderIngredientDataInfoUI.getItemName(): String {
    return name.takeIf { it.isNotEmpty() }.orIfNull { matnr }
}