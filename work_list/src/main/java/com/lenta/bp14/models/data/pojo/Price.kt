package com.lenta.bp14.models.data.pojo


data class Price(
        val priceWithDiscount: Int,
        val priceWithoutDiscount: Int,
        val priceOne: Int,
        val priceOneSellOut: Int,
        val priceTwo: Int,
        val priceTwoByStock: Int
) {
}