package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskBoxesRepository
import com.lenta.bp9.model.task.TaskBoxInfo
import com.lenta.bp9.model.task.TaskProductInfo

class MemoryTaskBoxesRepository : ITaskBoxesRepository {

    private val boxes: ArrayList<TaskBoxInfo> = ArrayList()

    override fun getBoxes(): List<TaskBoxInfo> {
        return boxes
    }

    override fun findBox(box: TaskBoxInfo): TaskBoxInfo? {
        return boxes.firstOrNull { it.materialNumber == box.materialNumber && it.boxNumber == box.boxNumber}
    }

    override fun findBoxOfProduct(productInfo: TaskProductInfo): TaskBoxInfo? {
        return boxes.firstOrNull { it.materialNumber == productInfo.materialNumber}
    }

    override fun addBox(box: TaskBoxInfo): Boolean {
        var index = -1
        for (i in boxes.indices) {
            if (box.materialNumber == boxes[i].materialNumber && box.boxNumber == boxes[i].boxNumber) {
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
        boxes.clear()
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
            if (box.materialNumber == boxInfo.materialNumber && box.boxNumber == boxInfo.boxNumber) {
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