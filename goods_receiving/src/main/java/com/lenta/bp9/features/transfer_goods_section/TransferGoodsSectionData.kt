package com.lenta.bp9.features.transfer_goods_section

import com.lenta.shared.utilities.databinding.Evenable

data class TransferGoodsSectionItem(
        val number: Int,
        val condition: String,
        val representative: String?,
        val ofGoods: String?,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}