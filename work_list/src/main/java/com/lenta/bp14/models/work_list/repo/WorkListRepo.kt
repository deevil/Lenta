package com.lenta.bp14.models.work_list.repo

import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.work_list.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

class WorkListRepo @Inject constructor(
        private val hyperHive: HyperHive
) : IWorkListRepo {

    val units: ZmpUtz07V001 by lazy { ZmpUtz07V001(hyperHive) } // Единицы измерения
    val productInfo: ZfmpUtz48V001 by lazy { ZfmpUtz48V001(hyperHive) } // Информация о товаре
    val eanInfo: ZmpUtz25V001 by lazy { ZmpUtz25V001(hyperHive) } // Информация о штрих-коде

    override suspend fun getCommonGoodInfoByEan(ean: String): CommonGoodInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext CommonGoodInfo(
                    ean = "4005489741143",
                    material = "000000000000000021",
                    name = "Р/к горбуша (Россия) 230/250г",
                    units = Uom.ST,
                    defaultQuantity = 1.0,
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

    override suspend fun loadAdditionalGoodInfo(good: Good): AdditionalGoodInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext AdditionalGoodInfo(
                    storagePlaces = "125635; 652148; 635894",
                    minStock = (10..50).random().toDouble(),
                    movement = Movement(
                            inventory = "19.07.19 (-25 шт.)",
                            arrival = "29.07.19 (+50 шт; Z5)"
                    ),
                    price = Price(
                            commonPrice = (110..140).random().toDouble(),
                            discountPrice = (80..100).random().toDouble()
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
                                quantity = (1..99).random().toDouble()
                        )
                    }
            )
        }
    }

    override suspend fun loadSalesStatistics(good: Good): SalesStatistics? {
        return withContext(Dispatchers.IO) {
            return@withContext SalesStatistics(
                    lastSaleDate = Date(),
                    daySales = (10..50).random(),
                    weekSales = (80..150).random(),
                    units = Uom.ST
            )
        }
    }

    override suspend fun loadComments(good: Good): List<String>? {
        return withContext(Dispatchers.IO) {
            val comments = MutableList((1..3).random()) {
                "Комментарий ${it + 1}"
            }
            comments.add(0, "Не выбран")

            return@withContext comments
        }
    }

}

interface IWorkListRepo {
    suspend fun getCommonGoodInfoByEan(ean: String): CommonGoodInfo?
    suspend fun loadAdditionalGoodInfo(good: Good): AdditionalGoodInfo?
    suspend fun loadSalesStatistics(good: Good): SalesStatistics?
    suspend fun loadComments(good: Good): List<String>?
}