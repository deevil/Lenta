package com.lenta.bp15.model.pojo

import com.lenta.bp15.features.task_card.TaskInfoUi
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
        val comment: String,
        var goods: List<Good>
) {

    private var startHashState = NO_HASH

    fun isEmptyGoodList(): Boolean {
        return goods.isEmpty()
    }

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

    fun convertToTaskInfoUi(): TaskInfoUi {
        return TaskInfoUi(
                type = type,
                name = secondLine,
                quantity = "$marksQuantity",
                description = description,
                comment = comment
        )
    }

    fun saveStartState() {
        if (isNotSavedStartState()) {
            startHashState = this.hashCode()
        }
    }

    fun isChanged(): Boolean {
        return if (!isNotSavedStartState()) {
            this.hashCode() != startHashState
        } else false
    }

    fun clearStartState() {
        startHashState = NO_HASH
    }

    private fun isNotSavedStartState(): Boolean {
        return startHashState == NO_HASH
    }

    companion object {
        private const val NO_HASH = -1
    }

}