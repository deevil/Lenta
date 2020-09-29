package com.lenta.bp12.model.pojo.extentions

import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.shared.utilities.orIfNull

fun Basket.addGood(good: Good, quantity: Double) {
    val goodsOneUnitVolume = good.getVolumeCorrespondingToUom()
    val goodWholeVolume = goodsOneUnitVolume.toFloat() * quantity.toFloat()
    if (freeVolume >= goodWholeVolume) {
        freeVolume -= (goodsOneUnitVolume * quantity)
        val oldQuantity = goods[good].orIfNull { ZERO_QUANTITY }
        val newQuantity = quantity + oldQuantity
        goods[good] = newQuantity
    }
}

fun Basket.deleteGood(good: Good) {
    val goodsOneUnitVolume = good.getVolumeCorrespondingToUom()
    val oldQuantity = goods[good].orIfNull { ZERO_QUANTITY }
    val volumeToReturnToBasket = oldQuantity.toFloat() * goodsOneUnitVolume.toFloat()
    val basketsFreeVolumePlusGoodsVolume = freeVolume + volumeToReturnToBasket
    if (basketsFreeVolumePlusGoodsVolume <= volume) {
        freeVolume += volumeToReturnToBasket
        goods.remove(good)
    }
}

fun Basket.deleteGoodByMarks(good: Good) {
    val quantityToDel = good.marks.count { mark ->
        this.index == mark.basketNumber
    }.toDouble()

    val oldQuantity = this.goods[good]
    oldQuantity?.let {
        val newQuantity = it - quantityToDel
        if (newQuantity == ZERO_QUANTITY) {
            this.deleteGood(good)
        } else {
            minusQuantityOfGood(good, quantityToDel, newQuantity)
        }
    }
}

fun Basket.minusQuantityOfGood(good: Good, quantityToDel: Double, newQuantity: Double) {
    val volumeToReturnToBasket = quantityToDel * good.getVolumeCorrespondingToUom()
    val freeVolumePlusGoodsVolume = freeVolume + volumeToReturnToBasket
    if (freeVolumePlusGoodsVolume <= volume) {
        this.goods[good] = newQuantity
        this.freeVolume += volumeToReturnToBasket
    }
}

fun Basket.getDescription(isDivBySection: Boolean, isWholeSale: Boolean): String {
    return buildString {
        val control = "${control?.codeInRus()}"

        if (!isWholeSale) {
            val sectionBlock = if (isDivBySection) "C-$section/" else ""
            append(sectionBlock)

            val goodTypeBlock = if (goodType.isNullOrEmpty()) "" else "$goodType/"
            append(goodTypeBlock)

            append(control)

            val providerBlock = if (provider?.code.isNullOrEmpty()) "" else "/ПП-${provider?.code}"

            append(providerBlock)

        } else {
            append(control)
        }

        val abbreviation = markTypeGroup?.abbreviation
        val markTypeGroupBlock = if (abbreviation.isNullOrEmpty()) "" else "/$abbreviation"

        append(markTypeGroupBlock)
    }
}

fun Basket.getGoodList(): List<Good> {
    return goods.keys.toList()
}

fun Basket.getSize(): Int {
    return getGoodList().size
}

fun Basket.getQuantityFromGoodList(): Int {
    return getGoodList().size
}

fun Basket.getQuantityOfGood(good: Good): Double {
    return goods[good] ?: 0.0
}

fun Basket?.getPosition(): Int {
    return this?.index ?: 0
}

/**
 * Показывает есть ли хоть одна не закрытая корзина
 * */
fun List<Basket>.isAnyNotLocked() = this.any { it.isLocked.not() }

/**
 * Показывает есть ли хоть одна распечатанная корзина
 * */
fun List<Basket>.isAnyPrinted() = this.any { it.isPrinted }