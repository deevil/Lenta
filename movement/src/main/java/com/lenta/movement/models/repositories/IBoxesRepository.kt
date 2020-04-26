package com.lenta.movement.models.repositories

import com.lenta.movement.models.ExciseBox
import com.lenta.movement.models.ExciseProductInfo

interface IBoxesRepository {
    fun getBoxesGroupByProduct(): Map<ExciseProductInfo, List<ExciseBox>>
    fun getBoxes(): List<ExciseBox>
    fun getBoxesByProduct(exciseProductInfo: ExciseProductInfo): List<ExciseBox>
    fun addBoxes(vararg boxes: ExciseBox)
    fun removeBox(box: ExciseBox)
    fun clear()
}