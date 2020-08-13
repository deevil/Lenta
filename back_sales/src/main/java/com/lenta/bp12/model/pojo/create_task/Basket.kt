package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.request.pojo.ProviderInfo

data class Basket(
        val index: Int,
        val section: String?,
        val goodType: String?,
        val control: ControlType?,
        val provider: ProviderInfo?,

        var isPrinted: Boolean = false,
        var isLocked: Boolean = false,
        var markedForLock: Boolean = false,

        val quantity: String? = "",
        val volume: Double = 0.0,
        val goods: MutableList<GoodCreate> = mutableListOf(),
        var freeVolume: Double = volume
) {



    fun addGood(good: GoodCreate){
        if (freeVolume >= good.volume) {
            freeVolume -= good.volume
            goods.add(good)
        }
    }

    fun getDescription2(isDivBySection: Boolean): String {
        val sectionBlock = if (isDivBySection) "C-${section}/" else ""
        return "$sectionBlock${goodType}/${control?.code}/ПП-${provider?.code}"
    }

    fun getDescription(isDivBySection: Boolean): String {

        return buildString {
            val sectionBlock = if (isDivBySection) "C-$section/" else ""
            append(sectionBlock)

            val goodTypeBlock = if(goodType.isNullOrEmpty()) "" else "$goodType/"
            append(goodTypeBlock)

            append("${control?.code}")

            val providerBlock = if(provider?.code.isNullOrEmpty()) "" else "/ПП-${provider?.code}"

            append(providerBlock)
        }
    }

}

enum class BasketState {
    NOT_LOCKED,
    PRE_LOCKED,
    LOCKED
}