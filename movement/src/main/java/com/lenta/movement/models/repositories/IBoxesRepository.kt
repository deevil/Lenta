package com.lenta.movement.models.repositories

import com.lenta.movement.models.ExciseBox
import com.lenta.movement.models.ProductInfo

interface IBoxesRepository {
    fun getBoxesGroupByProduct(): Map<ProductInfo, List<ExciseBox>>
    fun getBoxes(): List<ExciseBox>
    fun getBoxesByProduct(productInfo: ProductInfo): List<ExciseBox>
    fun addBoxes(vararg boxes: ExciseBox)
    fun removeBox(box: ExciseBox)
    fun clear()
}