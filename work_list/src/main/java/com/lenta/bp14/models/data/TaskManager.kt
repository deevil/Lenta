package com.lenta.bp14.models.data

import com.lenta.bp14.models.data.pojo.*
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants.DATE_FORMAT_ddmmyy
import java.text.SimpleDateFormat
import java.util.*


class TaskManager {

    lateinit var marketNumber: String

    var currentGood: Good?
    var currentTaskFilter: TaskFilter? = null


    init {
        currentGood = getTestGoodList(1)[0]
    }


    fun getTestTaskList(numberOfItems: Int): List<Task> {
        return List(numberOfItems) {
            Task(
                    id = it + 1,
                    type = "РБС - 30" + (0..9).random(),
                    name = "Рабочий список от ${(10..28).random()}.0${(1..9).random()}.19 ${(10..23).random()}:${(10..59).random()}",
                    status = when ((0..2).random()) {
                        1 -> TaskStatus.BLOCK
                        2 -> TaskStatus.SELF_BLOCK
                        else -> TaskStatus.STARTED
                    },
                    quantity = (1..99).random()
            )
        }
    }

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
            val goodStatus = if ((0..1).random() == 1) PriceTagStatus.MISSING else PriceTagStatus.WITH_ERROR
            val priceTagStatus = when ((0..2).random()) {
                0 -> PrintStatus.NOT_PRINTED
                else -> PrintStatus.PRINTED
            }

            Good(
                    id = it + 1,
                    material = "000000000000" + (111111..999999).random(),
                    ean = (11111111..999999999999).random().toString(),
                    name = "Товар ${it + (1..99).random()}",
                    uom = Uom.DEFAULT,
                    quantity = quantity,
                    priceTagStatus = if (quantity == 0) goodStatus else PriceTagStatus.CORRECT,
                    printStatus = priceTagStatus,
                    comments = getTestComments(),
                    shelfLives = getTestShelfLives(),
                    deliveries = getTestDeliveries(),
                    salesStatistics = getTestSalesStatistics(),
                    shelfLifeDays = (5..30).random(),
                    stocks = getTestStock(),
                    providers = getTestProviders(),
                    storagePlaces = "" + (100000..999999).random() + "; " + (100000..999999).random() + "; " + (100000..999999).random(),
                    minStock = (10..200).random(),
                    goodMovement = getTestGoodMovement(),
                    price = getTestPrice(),
                    promo = Promo("Какая то акция", "Период: 30.07.19 - 24.08.19"),
                    goodGroup = (111111..999999).random().toString(),
                    purchaseGroup = (1111..9999).random().toString(),
                    firstStorageStock = (10..50).random()
            )
        }
    }

    private fun getTestPrice(): Price {
        val priceWithDiscount = (100..999).random()
        return Price(
                priceWithDiscount = priceWithDiscount,
                priceWithoutDiscount = priceWithDiscount + (10..70).random(),
                priceOne = priceWithDiscount + (10..70).random(),
                priceTwo = priceWithDiscount + (10..70).random(),
                priceOneSellOut = priceWithDiscount - (10..70).random(),
                priceTwoByStock = priceWithDiscount - (10..70).random()
        )
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

    private fun getTestDeliveries(): MutableList<Delivery> {
        return MutableList((2..7).random()) {
            Delivery(
                    id = it + 1,
                    status = if ((0..1).random() == 0) DeliveryStatus.ORDERED else DeliveryStatus.ON_WAY,
                    additional = if ((0..1).random() == 0) "ПП" else "РЦ",
                    quantity = (1..99).random(),
                    date = Date()
            )
        }
    }

    private fun getTestSalesStatistics(): SalesStatistics? {
        return SalesStatistics(
                lastSaleDate = Date(),
                daySales = (50..200).random(),
                weekSales = (400..1000).random()
        )
    }

    private fun getTestStock(): MutableList<Stock> {
        return MutableList((5..9).random()) {
            Stock(
                    id = it + 1,
                    storageNumber = "0" + (0..9).random() + (0..9).random() + (0..9).random(),
                    quantity = (1..99).random()
            )
        }
    }

    private fun getTestProviders(): MutableList<Provider> {
        return MutableList((3..5).random()) {
            Provider(
                    id = it + 1,
                    code = (111111..999999).random().toString(),
                    name = "Поставщик ${it + 1}",
                    kipStart = Date(),
                    kipEnd = Date()
            )
        }
    }

    private fun getTestGoodMovement(): GoodMovement {
        return GoodMovement(
                inventory = "${(10..15).random()}.09.19 (-${(1..99).random()} шт.)",
                arrival = "${(16..28).random()}.09.19 (+${(50..99).random()} шт.)"
        )
    }

}