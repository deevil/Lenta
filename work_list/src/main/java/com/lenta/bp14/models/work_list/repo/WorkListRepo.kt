package com.lenta.bp14.models.work_list.repo

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.work_list.*
import com.lenta.bp14.platform.extentions.WorkListGoodInfo
import com.lenta.bp14.platform.extentions.toWorkListGoodInfo
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.dao_ext.getUnitName
import com.lenta.shared.fmp.resources.dao_ext.getItemsByTid
import com.lenta.shared.fmp.resources.dao_ext.toDescriptionsList
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz17V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class WorkListRepo @Inject constructor(
        private val hyperHive: HyperHive
) : IWorkListRepo {

    val units: ZmpUtz07V001 by lazy { ZmpUtz07V001(hyperHive) } // Единицы измерения
    val productInfo: ZfmpUtz48V001 by lazy { ZfmpUtz48V001(hyperHive) } // Информация о товаре
    val eanInfo: ZmpUtz25V001 by lazy { ZmpUtz25V001(hyperHive) } // Информация о штрих-коде
    val dictonary: ZmpUtz17V001 by lazy { ZmpUtz17V001(hyperHive) } // Справочник с наборами данных

    override suspend fun getGoodByMaterial(material: String): Good? {
        return withContext(Dispatchers.IO) {
            getWorkListGoodInfoByMaterial(material)?.let { workListGoodInfo ->
                val unitsName = getUnitsName(workListGoodInfo.unitsCode)
                val shelfLifeTypes = getShelfLifeTypes()
                val comments = getWorkListComments()

                return@withContext Good(
                        material = material,
                        name = workListGoodInfo.name,
                        units = Uom(
                                code = workListGoodInfo.unitsCode,
                                name = unitsName ?: ""
                        ),
                        goodGroup = workListGoodInfo.goodGroup,
                        purchaseGroup = workListGoodInfo.purchaseGroup,
                        shelfLife = workListGoodInfo.shelfLife,
                        remainingShelfLife = workListGoodInfo.remainingShelfLife,
                        shelfLifeType = MutableLiveData(shelfLifeTypes),
                        comments = MutableLiveData(comments),
                        options = GoodOptions(

                        )

                )
            }

            return@withContext null
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

    override suspend fun loadComments(good: Good): List<String>? {
        return withContext(Dispatchers.IO) {
            val comments = MutableList((1..3).random()) {
                "Комментарий ${it + 1}"
            }
            comments.add(0, "Не выбран")

            return@withContext comments
        }
    }

    override suspend fun getWorkListGoodInfoByMaterial(material: String?): WorkListGoodInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext productInfo.getProductInfoByMaterial(material)?.toWorkListGoodInfo()
        }
    }

    private suspend fun getShelfLifeTypes(): List<String> {
        return withContext(Dispatchers.IO) {
            return@withContext dictonary.getItemsByTid("007")?.toDescriptionsList() ?: listOf() // 007 - Типы сроков годности
        }
    }

    private suspend fun getWorkListComments(): List<String> {
        return withContext(Dispatchers.IO) {
            val comments = dictonary.getItemsByTid("019")?.toDescriptionsList()?.toMutableList() ?: mutableListOf() // 019 - Комментарии
            comments.add(0, "Не выбран")
            return@withContext comments
        }
    }

    private suspend fun getUnitsName(code: String?): String? {
        return withContext(Dispatchers.IO) {
            return@withContext units.getUnitName(code)?.toLowerCase(Locale.getDefault())
        }
    }

}

interface IWorkListRepo {
    suspend fun getGoodByMaterial(material: String): Good?

    suspend fun getWorkListGoodInfoByMaterial(material: String?): WorkListGoodInfo?


    suspend fun loadAdditionalGoodInfo(good: Good): AdditionalGoodInfo?
    suspend fun loadComments(good: Good): List<String>?
}