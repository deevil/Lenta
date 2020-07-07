package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.request.pojo.ProviderInfo

data class Basket(
        val section: String,
        val goodType: String,
        val control: ControlType,
        val provider: ProviderInfo
) {

    fun getDescription(isDivBySection: Boolean): String {
        val sectionBlock = if (isDivBySection) "C-${section}/" else ""
        return "$sectionBlock${goodType}/${control.code}/ПП-${provider?.code}"
    }

}