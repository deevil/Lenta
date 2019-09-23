package com.lenta.bp9.features.goods_details

import com.lenta.shared.utilities.databinding.Evenable

data class GoodsDetailsCategoriesItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val typeDiscrepancies: String,
        val uomName: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}