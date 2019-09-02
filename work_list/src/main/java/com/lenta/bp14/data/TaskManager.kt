package com.lenta.bp14.data

import com.lenta.bp14.data.model.*
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants.DATE_FORMAT_ddmmyy
import java.text.SimpleDateFormat
import java.util.*


class TaskManager {

    lateinit var marketNumber: String

    var currentGood: Good? = null
    var currentTaskFilter: TaskFilter? = null


    fun setCurrentTaskFilter(
            taskType: TaskType,
            goodName: String?,
            sectionNumber: String?,
            goodsGroup: String?,
            publicationDate: String?
    ) {
        currentTaskFilter = TaskFilter(
                taskType = taskType,
                goodName = if (goodName?.isNotEmpty() == true) goodName else null,
                sectionNumber = if (sectionNumber?.isNotEmpty() == true) sectionNumber.toInt() else null,
                goodsGroup = if (goodsGroup?.isNotEmpty() == true) goodsGroup else null,
                publicationDate = if (publicationDate?.length == DATE_FORMAT_ddmmyy.length) {
                    SimpleDateFormat(DATE_FORMAT_ddmmyy, Locale.getDefault()).parse(publicationDate)
                } else null
        )
    }


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
                    priceTagStatus = priceTagStatus,
                    shelfLives = getTestShelfLives(),
                    comments = getTestComments()
            )
        }
    }

    private fun getTestShelfLives(): MutableList<ShelfLife> {
        return MutableList((2..7).random()) {
            ShelfLife(
                    id = it + 1,
                    shelfLife = Date(),
                    publicationDate = if ((0..1).random() == 1) Date() else null,
                    quantity = (1..99).random()
            )
        }
    }

    private fun getTestComments(): MutableList<Comment> {
        return MutableList((2..7).random()) {
            Comment(
                    id = it + 1,
                    message = if ((0..1).random() == 1) "С товаром все хорошо" else "С товаром все плохо",
                    quantity = (1..99).random()
            )
        }
    }

}