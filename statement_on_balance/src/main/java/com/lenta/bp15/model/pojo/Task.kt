package com.lenta.bp15.model.pojo

import com.lenta.bp15.features.task_card.TaskCardUi
import com.lenta.bp15.features.task_list.ItemTaskUi
import com.lenta.bp15.repository.database.pojo.TaskType
import com.lenta.bp15.repository.requests.pojo.MarkRawInfo
import com.lenta.shared.utilities.extentions.toSapBooleanString

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
        var isNotFinished: Boolean,
        val comment: String,
        var goods: MutableList<Good> = mutableListOf()
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
                isNotFinished = isNotFinished,
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

    private fun isNotSavedStartState(): Boolean {
        return startHashState == NO_HASH
    }

    fun getCodeWithName(): String {
        return "$firstLine / $secondLine"
    }

    fun getAllMarks(): Map<String, Mark> {
        val allMarks = mutableMapOf<String, Mark>()
        goods.map { good ->
            allMarks.putAll(good.marks)
        }

        return allMarks
    }

    fun updateGood(good: Good) {
        goods.remove(goods.find { it.material == good.material })
        goods.add(good)
    }

    fun getProcessedMarkListForSave(): List<MarkRawInfo> {
        return goods.map { good ->
            good.marks.filter { it.value.isScan }.map {
                val mark = it.value
                MarkRawInfo(
                        material = mark.material,
                        number = mark.number,
                        isScan = mark.isScan.toSapBooleanString()
                )
            }
        }.flatten()
    }

    companion object {
        private const val NO_HASH = -1
    }

}