package com.lenta.bp14.models.work_list.repo

import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.work_list.*
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random

class WorkListRepo {

    suspend fun getCommonGoodInfoByEan(ean: String): CommonGoodInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext CommonGoodInfo(
                    ean = "11111111",
                    material = "000000000000222222",
                    matcode = "333333333333",
                    name = "Товар",
                    units = Uom.ST,
                    defaultQuantity = BigDecimal.ONE,
                    goodGroup = "123456",
                    purchaseGroup = "1111",
                    shelfLife = (3..14).random(),
                    options = GoodOptions(
                            matrixType = MatrixType.Active,
                            section = "5",
                            goodType = GoodType.COMMON
                    )
            )
        }
    }

    suspend fun loadAdditionalGoodInfo(good: Good): AdditionalGoodInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext AdditionalGoodInfo(
                    storagePlaces = "125635; 652148; 635894",
                    minStock = (10..50).random().toBigDecimal(),
                    movement = Movement(
                            inventory = "19.07.19 (-25 шт.)",
                            arrival = "29.07.19 (+50 шт; Z5)"
                    ),
                    price = Price(
                            commonPrice = (110..140).random().toBigDecimal(),
                            discountPrice = (80..100).random().toBigDecimal()
                    ),
                    promo = Promo(
                            name = "Распродажа кукурузы ТК 0007",
                            period = "Период 30.05.19 - 12.09.19"
                    ),
                    providers = MutableList((3..5).random()) {
                        Provider(
                                number = it + 1,
                                code = (111111..999999).random().toString(),
                                name = "Поставщик ${it + 1}",
                                kipStart = Date(),
                                kipEnd = Date()
                        )
                    },
                    stocks = MutableList((5..9).random()) {
                        Stock(
                                number = it + 1,
                                storage = "0" + (0..9).random() + (0..9).random() + (0..9).random(),
                                quantity = (1..99).random().toBigDecimal()
                        )
                    }
            )
        }
    }

    suspend fun loadSalesStatistics(good: Good): SalesStatistics? {
        return withContext(Dispatchers.IO) {
            return@withContext SalesStatistics(
                    lastSaleDate = Date(),
                    daySales = (10..50).random(),
                    weekSales = (80..150).random(),
                    units = Uom.ST
            )
        }
    }

    suspend fun loadDeliveries(good: Good): List<Delivery>? {
        return withContext(Dispatchers.IO) {
            return@withContext List((3..5).random()) {
                Delivery(
                        status = if (Random.nextBoolean()) DeliveryStatus.ORDERED else DeliveryStatus.ON_WAY,
                        info = if (Random.nextBoolean()) "ПП" else "РЦ",
                        quantity = (1..99).random().toBigDecimal(),
                        units = Uom.KAR,
                        date = Date()
                )
            }
        }
    }

    suspend fun loadComments(good: Good): List<String>? {
        return withContext(Dispatchers.IO) {
            val comments = MutableList((1..3).random()) {
                "Комментарий ${it + 1}"
            }
            comments.add(0, "Не выбран")

            return@withContext comments
        }
    }

}