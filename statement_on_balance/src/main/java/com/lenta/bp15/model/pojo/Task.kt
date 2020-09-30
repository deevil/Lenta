package com.lenta.bp15.model.pojo

import com.lenta.bp15.features.task_card.TaskCardUi
import com.lenta.bp15.features.task_list.ItemTaskUi

data class Task(
        val number: String,
        val type: String,
        val firstLine: String,
        val secondLine: String,
        val title: String,
        val description: String,
        val goodsQuantity: Int,
        val marksQuantity: Int,
        val block: Block,
        val isFinished: Boolean,
        val comment: String
) {

    fun convertToItemTaskUi(index: Int): ItemTaskUi {
        return ItemTaskUi(
                position = "${index + 1}",
                number = number,
                firstLine = firstLine,
                secondLine = secondLine,
                isFinished = isFinished,
                lockType = block.type,
                goodsQuantity = "$goodsQuantity"
        )
    }

    fun convertToTaskCardUi(): TaskCardUi {
        return TaskCardUi(
                type = type,
                name = secondLine,
                quantity = "$marksQuantity",
                description = description,
                comment = comment
        )
    }

}