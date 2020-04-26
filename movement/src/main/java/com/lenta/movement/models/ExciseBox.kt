package com.lenta.movement.models

data class ExciseBox(
    val code: String,
    val productInfo: ExciseProductInfo,
    val stamps: List<ExciseStamp>
)