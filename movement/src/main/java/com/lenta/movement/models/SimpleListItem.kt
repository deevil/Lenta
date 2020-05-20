package com.lenta.movement.models

import com.lenta.shared.utilities.databinding.Evenable

data class SimpleListItem(
    val number: Int,
    val title: String,
    val subtitle: String = "",
    val countWithUom: String
) : Evenable {

    override fun isEven() = (number - 1) % 2 == 0

}