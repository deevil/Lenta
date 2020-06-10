package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.request.pojo.ProviderInfo

data class Basket(
        val section: String,
        val matype: String,
        val control: ControlType,
        val provider: ProviderInfo? = null
) {

    fun getDescription(): String {
        return "C-${section}/${matype}/${control.code}/ПП-${provider?.code}"
    }

}