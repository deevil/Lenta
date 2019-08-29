package com.lenta.bp14.data

import com.lenta.bp14.data.model.Good
import com.lenta.shared.models.core.Uom


class TaskManager {

    var currentGood: Good? = null

    fun getTestGoodList(numberOfItems: Int): List<Good> {
        return List(numberOfItems) {
            Good(
                    id = it + 1,
                    material = "000000000000" + (111111..999999).random(),
                    name = "Товар ${it + (1..99).random()}",
                    uom = Uom.DEFAULT
            )
        }
    }

}