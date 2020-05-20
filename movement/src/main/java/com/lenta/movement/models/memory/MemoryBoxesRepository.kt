package com.lenta.movement.models.memory

import com.lenta.movement.models.ExciseBox
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.models.repositories.IBoxesRepository

class MemoryBoxesRepository: IBoxesRepository {

    private val boxes: MutableList<ExciseBox> = mutableListOf()

    override fun getBoxesGroupByProduct(): Map<ProductInfo, List<ExciseBox>> {
        return boxes.groupBy { it.productInfo }
    }

    override fun getBoxes(): List<ExciseBox> {
        return boxes
    }

    override fun getBoxesByProduct(productInfo: ProductInfo): List<ExciseBox> {
        return boxes.filter { box ->
            box.productInfo.materialNumber == productInfo.materialNumber
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