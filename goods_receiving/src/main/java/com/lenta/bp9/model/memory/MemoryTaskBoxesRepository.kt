package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskBoxesRepository
import com.lenta.bp9.model.task.TaskBoxInfo
import com.lenta.bp9.model.task.TaskProductInfo

class MemoryTaskBoxesRepository : ITaskBoxesRepository {

    private val boxes: ArrayList<TaskBoxInfo> = ArrayList()

    override fun getTaskBoxes(): List<TaskBoxInfo> {
        return boxes
    }

    override fun findBox(box: TaskBoxInfo): TaskBoxInfo? {
        return findBox(box.boxNumber)
    }

    override fun findBox(boxNumber: String): TaskBoxInfo? {
        return boxes.firstOrNull { it.boxNumber == boxNumber }
    }

    override fun findBoxesOfProduct(productInfo: TaskProductInfo): List<TaskBoxInfo>? {
        return boxes.filter { it.materialNumber == productInfo.materialNumber}
    }

    override fun addBox(box: TaskBoxInfo): Boolean {
        var index = -1
        for (i in boxes.indices) {
            if (box.boxNumber == boxes[i].boxNumber) {
                index = i
            }
        }

        if (index == -1) {
            boxes.add(box)
            return true
        }
        return false
    }

    override fun updateBoxes(newBoxes: List<TaskBoxInfo>) {
        clear()
        newBoxes.map {
            addBox(it)
        }
    }

    override fun changeBoxes(box: TaskBoxInfo): Boolean {
        deleteBox(box)
        return addBox(box)
    }

    override fun deleteBox(box: TaskBoxInfo): Boolean {
        boxes.map { it }.filter {boxInfo ->
            if (box.boxNumber == boxInfo.boxNumber) {
                boxes.remove(boxInfo)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun deleteBoxes(delBoxes: List<TaskBoxInfo>): Boolean {
        return boxes.removeAll(delBoxes)
    }

    override fun deleteBoxesForProduct(product: TaskProductInfo): Boolean {
        boxes.map { it }.filter {boxInfo ->
            if (boxInfo.materialNumber == product.materialNumber) {
                boxes.remove(boxInfo)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun clear() {
        boxes.clear()
    }
}