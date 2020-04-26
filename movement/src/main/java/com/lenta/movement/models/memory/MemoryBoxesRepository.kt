package com.lenta.movement.models.memory

import com.lenta.movement.models.ExciseBox
import com.lenta.movement.models.ExciseProductInfo
import com.lenta.movement.models.repositories.IBoxesRepository

class MemoryBoxesRepository: IBoxesRepository {

    private val boxes: MutableList<ExciseBox> = mutableListOf()

    override fun getBoxesGroupByProduct(): Map<ExciseProductInfo, List<ExciseBox>> {
        return boxes.groupBy { it.productInfo }
    }

    override fun getBoxes(): List<ExciseBox> {
        return boxes
    }

    override fun getBoxesByProduct(exciseProductInfo: ExciseProductInfo): List<ExciseBox> {
        return boxes.filter { box ->
            box.productInfo.materialNumber == exciseProductInfo.materialNumber
        }
    }

    override fun addBoxes(vararg boxes: ExciseBox) {
        this.boxes.addAll(boxes.toList())
    }

    override fun removeBox(box: ExciseBox) {
        boxes.remove(box)
    }

    override fun clear() {
        boxes.clear()
    }

}