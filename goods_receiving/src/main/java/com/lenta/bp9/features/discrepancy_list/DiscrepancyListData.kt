package com.lenta.bp9.features.discrepancy_list

import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.shared.utilities.databinding.Evenable

data class GoodsDiscrepancyItem(
        val number: Int,
        val name: String,
        val uomName: String,
        val countAccept: Double,
        val countRefusal: Double,
        val productInfo: TaskProductInfo?,
        val batchInfo: TaskBatchInfo?,
        val even: Boolean
) : Evenable {
    override fun isEven() = even
}