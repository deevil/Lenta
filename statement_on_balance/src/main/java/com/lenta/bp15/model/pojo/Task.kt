package com.lenta.bp15.model.pojo

import com.lenta.bp15.features.task_card.TaskCardUi
import com.lenta.bp15.features.task_list.ItemTaskUi
import com.lenta.bp15.repository.database.pojo.TaskType

data class Task(
        val number: String,
        var type: TaskType,
        val firstLine: String,
        val secondLine: String,
        val title: String,
        val description: String,
        val goodsQuantity: Int,
        val marksQuantity: Int,
        val block: Block,
        val isFinished: Boolean,
        val comment: String,
        var goods: List<Good> = emptyList()
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

    fun convertToTaskInfoUi(): TaskCardUi {
        return TaskCardUi(
                typeName = type.name,
                taskName = secondLine,
                quantity = "$marksQuantity",
                description = type.description,
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

    /*fun clearStartState() {
        startHashState = NO_HASH
    }*/

    private fun isNotSavedStartState(): Boolean {
        return startHashState == NO_HASH
    }

    companion object {
        private const val NO_HASH = -1
    }

}