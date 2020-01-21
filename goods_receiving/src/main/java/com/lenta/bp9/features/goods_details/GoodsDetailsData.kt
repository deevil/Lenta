package com.lenta.bp9.features.goods_details

import com.lenta.shared.utilities.databinding.Evenable

data class GoodsDetailsCategoriesItem(
        val number: Int,
        val name: String,
        val quantityWithUom: String,
        val isNormDiscrepancies: Boolean,
        val typeDiscrepancies: String,
        val materialNumber: String,
        val batchNumber: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}