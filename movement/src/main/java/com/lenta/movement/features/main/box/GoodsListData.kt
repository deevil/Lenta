package com.lenta.movement.features.main.box

import com.lenta.shared.utilities.databinding.Evenable

data class GoodListItem(
    val number: Int,
    val name: String,
    val countWithUom: String
): Evenable {

    override fun isEven() = (number - 1) % 2 == 0

}