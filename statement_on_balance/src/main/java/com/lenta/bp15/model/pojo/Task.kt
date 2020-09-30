package com.lenta.bp15.model.pojo

import com.lenta.bp15.features.task_list.ItemTaskUi
import com.lenta.shared.utilities.extentions.dropZeros

data class Task(
        val number: String,
        val name: String,
        val description: String,
        val quantity: Double,
        val block: Block,
        val isFinished: Boolean
) {

    fun convertToItemTaskUi(index: Int): ItemTaskUi {
        return ItemTaskUi(
                position = "${index + 1}",
                number = number,
                name = name,
                description = description,
                isFinished = isFinished,
                lockType = block.type,
                quantity = quantity.dropZeros()
        )
    }

}