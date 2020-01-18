package com.lenta.bp9.features.discrepancy_list

import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.shared.utilities.databinding.Evenable

data class GoodsDiscrepancyItem(
        val number: Int,
        val name: String,
        val countRefusalWithUom: String,
        val quantityNotProcessedWithUom: String,
        val discrepanciesName: String,
        val productInfo: TaskProductInfo?,
        val productDiscrepancies: TaskProductDiscrepancies?,
        val batchInfo: TaskBatchInfo?,
        val even: Boolean
) : Evenable {
    override fun isEven() = even
}