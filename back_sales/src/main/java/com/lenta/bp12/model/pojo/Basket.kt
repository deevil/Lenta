package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.GoodType
import com.lenta.bp12.request.pojo.ProviderInfo

data class Basket(
        val section: String,
        val type: GoodType,
        val control: ControlType,
        val provider: ProviderInfo? = null
)