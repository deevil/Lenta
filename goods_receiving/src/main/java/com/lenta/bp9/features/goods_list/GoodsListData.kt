package com.lenta.bp9.features.goods_list

import com.lenta.bp9.model.task.TaskBatchesInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.shared.utilities.databinding.Evenable

data class ListCountedItem(
        val number: Int,
        val name: String,
        val uomName: String,
        val countAccept: Double,
        val countRefusal: Double,
        val productInfo: TaskProductInfo?,
        val batchInfo: TaskBatchesInfo?,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}

data class ListWithoutBarcodeItem(
        val number: Int,
        val name: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}