package com.lenta.bp12.model.pojo.extentions

import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.model.ScreenStatus
import com.lenta.bp12.model.pojo.*
import com.lenta.shared.utilities.extentions.sumWith

fun Good.addPosition(position: Position) {
    positions.find { it.provider.code == position.provider.code }?.let { found ->
        found.quantity = found.quantity.sumWith(position.quantity)
    } ?: positions.add(position)
}

fun Good.addMark(mark: Mark) {
    if (marks.find { it.number == mark.number } == null) {
        marks.add(mark)
    }
}

fun Good.addMarks(marksToAdd: List<Mark>) {
    marksToAdd.forEach { mark ->
        if (marks.find { it.number == mark.number } == null && !marks.contains(mark)) {
            marks.add(mark)
        }
    }
}

fun Good.addPart(part: Part) {
    parts.find {
        it.providerCode == part.providerCode &&
                it.producerCode == part.producerCode &&
                it.date == part.date
    }?.let { found ->
        found.quantity = found.quantity.sumWith(part.quantity)
    } ?: parts.add(part)
}

fun Good.removePartsMarksPositionsByBasketIndex(basketIndex: Int){
    removeMarksByBasketIndex(basketIndex)
    removePartsByBasketNumber(basketIndex)
    removePositionsByBasketIndex(basketIndex)
}

fun Good.removePartsByBasketNumber(basketIndex: Int) {
    parts.removeAll { it.basketNumber == basketIndex }
}

fun Good.removeMarksByBasketIndex(basketIndex: Int) {
    marks.removeAll { it.basketNumber == basketIndex }
}
/**
 * Когда мы сканируем марку или партию, мы так же добавляем пустые позиции в список позиций.
 * Этот метод удаляет эти позиции (потому что они привязаны к номеру корзины)
 * */
fun Good.removePositionsByBasketIndex(basketIndex: Int) {
    positions.removeAll { it.basketNumber == basketIndex }
}

fun Good.removeAllMark() {
    marks.clear()
}

fun Good.removeAllPart() {
    parts.clear()
}
/**
 * Метод уменьшает количество позиций у товара, лежащего в общем списке, когда его удаляют из корзины.
 * (эти позиции не привязаны к номеру корзины)
 * */
fun Good.deletePositionsFromTask(goodFromBasket: Good, basketToGetQuantity: Basket) {
    //Найдем у этого товара позиции с подходящим количеством
    val positionThatFits = positions.firstOrNull { positionFromTask ->
        goodFromBasket.positions.any { it.quantity >= positionFromTask.quantity }
    }

    positionThatFits?.let {
        //Получим количество позиций этого товара
        val quantityOfPositionFromTask = it.quantity
        //Получим количество удаляемого товара из корзины
        val quantityToMinus = basketToGetQuantity.goods[goodFromBasket] ?: 0.0

        //Отнимем первое от второго и вернем в товар
        val newQuantity = quantityOfPositionFromTask.minus(quantityToMinus)

        it.quantity = newQuantity
        val index = positions.indexOf(it)
        positions.set(index, it)

    }
}

fun Good.removeMarks(other: List<Mark>) = this.marks.removeAll(other)

fun Good.getScreenStatus(): ScreenStatus {
    return when (this.kind) {
        GoodKind.COMMON -> ScreenStatus.COMMON
        GoodKind.ALCOHOL -> ScreenStatus.ALCOHOL
        GoodKind.EXCISE -> ScreenStatus.EXCISE
        GoodKind.MARK -> ScreenStatus.MARK
        else -> ScreenStatus.VET
    }
}

fun Good.isGoodHasSameEan(otherEan: String): Boolean {
    return this.ean == otherEan || this.eans.contains(otherEan)
}

fun Good.isGoodHasSameMaxRetailPrice(otherMrc: String) = this.maxRetailPrice == otherMrc

fun Good.clearMarksPartsPositions() {
    this.marks.clear()
    this.parts.clear()
    this.positions.clear()
}