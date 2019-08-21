package com.lenta.bp9.features.goods_list

import com.lenta.shared.utilities.databinding.Evenable

data class GoodsListCountedItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}