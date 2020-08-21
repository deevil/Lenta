package com.lenta.bp12.platform.extention

import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.model.pojo.create_task.GoodCreate
import com.lenta.shared.utilities.orIfNull

fun Basket.addGood(good: GoodCreate, quantity: Double) {
    if (freeVolume >= good.volume * quantity) {
        freeVolume -= (good.volume * quantity)
        val oldQuantity = goods[good].orIfNull { 0.0 }
        val newQuantity = quantity + oldQuantity
        goods[good] = newQuantity
    }
}

fun Basket.deleteGood(good: GoodCreate) {
    if (freeVolume + good.volume <= volume) {
        val oldQuantity = goods[good].orIfNull { 0.0 }
        val volumeToReturnToBasket = oldQuantity * good.volume
        freeVolume += volumeToReturnToBasket
        goods.remove(good)
    }
}

fun Basket.getDescription(isDivBySection: Boolean): String {

    return buildString {
        val sectionBlock = if (isDivBySection) "C-$section/" else ""
        append(sectionBlock)

        val goodTypeBlock = if (goodType.isNullOrEmpty()) "" else "$goodType/"
        append(goodTypeBlock)

        append("${control?.code}")

        val providerBlock = if (provider?.code.isNullOrEmpty()) "" else "/ПП-${provider?.code}"

        append(providerBlock)
    }
}

fun Basket.getGoodList() : List<GoodCreate> {
    return goods.keys.toList()
}

fun Basket.getSize() : Int {
    return getGoodList().size
}