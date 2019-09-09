package com.lenta.bp14.data.model


data class Price(
        val priceWithDiscount: Int,
        val priceWithoutDiscount: Int,
        val priceOne: Int,
        val priceOneSellOut: Int,
        val priceTwo: Int,
        val priceTwoByStock: Int
) {
}