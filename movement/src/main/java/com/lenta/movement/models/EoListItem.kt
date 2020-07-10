package com.lenta.movement.models

import com.lenta.shared.utilities.databinding.Evenable

data class EoListItem(
    val number: Int,
    val title: String,
    val subtitle: String = "",
    val quantity: String,
    val isClickable: Boolean,
    val stateResId: Int?

) : Evenable {

    override fun isEven() = (number - 1) % 2 == 0

}