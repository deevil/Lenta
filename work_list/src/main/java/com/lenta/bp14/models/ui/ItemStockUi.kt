package com.lenta.bp14.models.ui

data class ItemStockUi(
        val number: String,
        val storage: String,
        val quantity: String,
        val zPartsQuantity: String = ""
)