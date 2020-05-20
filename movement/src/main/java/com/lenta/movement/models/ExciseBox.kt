package com.lenta.movement.models

data class ExciseBox(
    val code: String,
    val productInfo: ProductInfo,
    val stamps: List<ExciseStamp>
)