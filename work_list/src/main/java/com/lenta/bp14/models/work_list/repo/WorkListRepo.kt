package com.lenta.bp14.models.work_list.repo

import com.lenta.bp14.models.data.getGoodType
import com.lenta.bp14.models.work_list.Good
import com.lenta.bp14.models.work_list.GoodOptions
import com.lenta.bp14.platform.extentions.WorkListGoodInfo
import com.lenta.bp14.platform.extentions.toWorkListGoodInfo
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz17V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.isSapTrue
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
    val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки

    override suspend fun getGoodByMaterial(material: String): Good? {
        return withContext(Dispatchers.IO) {
            getGoodInfoByMaterial(material)?.let { goodInfo ->
                val ean = getEanByMaterial(material)
                val unitsCode = if (goodInfo.unitsCode == Uom.G.code) Uom.KG.code else goodInfo.unitsCode
                val unitsName = getUnitsName(unitsCode)
                val shelfLifeTypes = getShelfLifeTypes()
                val comments = getWorkListComments()

                return@withContext Good(
                        ean = ean,
                        material = material,
                        name = goodInfo.name,
                        units = Uom(
                                code = unitsCode,
                                name = unitsName ?: ""
                        ),
                        goodGroup = goodInfo.goodGroup,
                        purchaseGroup = goodInfo.purchaseGroup,
                        shelfLife = goodInfo.shelfLife,
                        remainingShelfLife = goodInfo.remainingShelfLife,
                        shelfLifeTypes = shelfLifeTypes,
                        comments = comments,
                        options = GoodOptions(
                                matrixType = getMatrixType(goodInfo.matrixType),
                                section = goodInfo.section,
                                goodType = getGoodType(
                                        alcohol = goodInfo.isAlcohol,
                                        excise = goodInfo.isExcise,
                                        marked = goodInfo.isMark),
                                healthFood = goodInfo.healthFood.isSapTrue(),
                                novelty = goodInfo.novelty.isSapTrue()
                        )
                )
            }

            return@withContext null
        }
    }

    override suspend fun getGoodByEan(ean: String): Good? {
        return withContext(Dispatchers.IO) {
            getMaterialByEan(ean)?.let { material ->
                getGoodInfoByMaterial(material)?.let { goodInfo ->
                    val unitsCode = if (goodInfo.unitsCode == Uom.G.code) Uom.KG.code else goodInfo.unitsCode
                    val unitsName = getUnitsName(unitsCode)
                    val shelfLifeTypes = getShelfLifeTypes()
                    val comments = getWorkListComments()

                    return@withContext Good(
                            ean = ean,
                            material = material,
                            name = goodInfo.name,
                            units = Uom(
                                    code = unitsCode,
                                    name = unitsName ?: ""
                            ),
                            goodGroup = goodInfo.goodGroup,
                            purchaseGroup = goodInfo.purchaseGroup,
                            shelfLife = goodInfo.shelfLife,
                            remainingShelfLife = goodInfo.remainingShelfLife,
                            shelfLifeTypes = shelfLifeTypes,
                            comments = comments,
                            options = GoodOptions(
                                    matrixType = getMatrixType(goodInfo.matrixType),
                                    section = goodInfo.section,
                                    goodType = getGoodType(
                                            alcohol = goodInfo.isAlcohol,
                                            excise = goodInfo.isExcise,
                                            marked = goodInfo.isMark),
                                    healthFood = goodInfo.healthFood.isSapTrue(),
                                    novelty = goodInfo.novelty.isSapTrue()
                            )
                    )
                }
            }

            return@withContext null
        }
    }

    override suspend fun getGoodInfoByMaterial(material: String?): WorkListGoodInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext productInfo.getProductInfoByMaterial(material)?.toWorkListGoodInfo()
        }
    }

    override suspend fun getMaterialByEan(ean: String?): String? {
        return withContext(Dispatchers.IO) {
            return@withContext eanInfo.getEanInfo(ean)?.toEanInfo()?.materialNumber
        }
    }

    override suspend fun getEanByMaterial(material: String?): String? {
        return withContext(Dispatchers.IO) {
            return@withContext eanInfo.getEanInfoFromMaterial(material)?.toEanInfo()?.ean
        }
    }

    private suspend fun getShelfLifeTypes(): List<DictElement> {
        return withContext(Dispatchers.IO) {
            val selfLives = dictonary.getItemsByTid("007")?.toElementList()?.toMutableList()
                    ?: mutableListOf() // 007 - Типы сроков годности
            selfLives.sortBy { it.order }
            Logg.d { "--> selfLives: $selfLives" }
            return@withContext selfLives
        }
    }

    private suspend fun getWorkListComments(): List<DictElement> {
        return withContext(Dispatchers.IO) {
            val comments = dictonary.getItemsByTid("019")?.toElementList()?.toMutableList()
                    ?: mutableListOf() // 019 - Комментарии
            comments.add(0, DictElement(
                    code = "0",
                    order = "0",
                    description = "Не выбран"
            ))
            comments.sortBy { it.order }
            Logg.d { "--> comments: $comments" }
            return@withContext comments
        }
    }

    private suspend fun getUnitsName(code: String?): String? {
        return withContext(Dispatchers.IO) {
            return@withContext units.getUnitName(code)?.toLowerCase(Locale.getDefault())
        }
    }

    override suspend fun getMaxQuantity(): Double? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getMaxQuantityProdWkl()
        }
    }

}

interface IWorkListRepo {
    suspend fun getGoodByMaterial(material: String): Good?
    suspend fun getGoodByEan(ean: String): Good?
    suspend fun getGoodInfoByMaterial(material: String?): WorkListGoodInfo?
    suspend fun getMaterialByEan(ean: String?): String?
    suspend fun getEanByMaterial(material: String?): String?
    suspend fun getMaxQuantity(): Double?
}