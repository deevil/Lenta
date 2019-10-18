package com.lenta.bp9.features.list_goods_transfer

import com.lenta.shared.utilities.databinding.Evenable

data class ListGoodsTransferItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}