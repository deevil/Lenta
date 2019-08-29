package com.lenta.bp14.data

import com.lenta.bp14.data.model.Good
import com.lenta.bp14.data.model.GoodStatus
import com.lenta.bp14.data.model.PriceTagStatus
import com.lenta.shared.models.core.Uom


class TaskManager {

    var currentGood: Good? = null

    fun getTestGoodList(numberOfItems: Int): List<Good> {
        return List(numberOfItems) {
            val quantity = (0..5).random()
            val goodStatus = if ((0..1).random() == 1) GoodStatus.MISSING_RIGHT else GoodStatus.MISSING_WRONG
            val priceTagStatus = when ((0..2).random()) {
                0 -> PriceTagStatus.NO_PRICE_TAG
                1 -> PriceTagStatus.WITH_ERROR
                else -> PriceTagStatus.PRINTED
            }

            Good(
                    id = it + 1,
                    material = "000000000000" + (111111..999999).random(),
                    name = "Товар ${it + (1..99).random()}",
                    uom = Uom.DEFAULT,
                    quantity = quantity,
                    goodStatus = if (quantity == 0) goodStatus else GoodStatus.PRESENT,
                    priceTagStatus = priceTagStatus
            )
        }
    }

}