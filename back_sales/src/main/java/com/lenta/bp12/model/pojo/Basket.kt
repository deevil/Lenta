package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.request.pojo.ProviderInfo

data class Basket(
        val section: String,
        val type: String,
        val control: ControlType,
        val provider: ProviderInfo? = null
) {

    fun getDescription(): String {
        return "C-${section}/${type}/${control.code}/ПП-${provider?.code}"
    }

}