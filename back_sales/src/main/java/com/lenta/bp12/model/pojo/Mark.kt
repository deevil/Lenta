package com.lenta.bp12.model.pojo

data class Mark(
        var number: String,
        var material: String,
        var boxNumber: String = "",
        var isBadMark: Boolean,
        var providerCode: String,
        var producerCode: String
)