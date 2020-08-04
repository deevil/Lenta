package com.lenta.bp12.model.pojo

import com.lenta.bp12.request.pojo.ProviderInfo

data class Position(
        var quantity: Double = 0.0,
        var provider: ProviderInfo
)