package com.lenta.movement.models

import com.lenta.shared.utilities.databinding.Evenable

data class TaskListItem(
    val number: Int,
    val title: String,
    val subtitle: String,
    val quantity: String,
    val isClickable: Boolean,
    val blockTypeResId1: Int?,
    val isNotFinish: Boolean, //по заданию отображать пиктограмму «Обработка начата»
    val isCons: Boolean //по заданию отображать пиктограмму «Требуется комплектация»

) : Evenable {

    override fun isEven() = (number - 1) % 2 == 0

}