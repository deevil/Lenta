package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.request.pojo.ProviderInfo

data class Position(
        var quantity: Double = 0.0,
        val innerQuantity: Double = 0.0,
        var provider: ProviderInfo?,
        var date: String?
)