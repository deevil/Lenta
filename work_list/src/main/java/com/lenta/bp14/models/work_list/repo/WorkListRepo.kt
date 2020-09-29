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
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.requests.combined.scan_info.pojo.EanInfo
import com.lenta.shared.utilities.extentions.getQuantity
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
                val defaultUnits = Uom(code = goodInfo.unitsCode, name = getUnitsName(goodInfo.unitsCode))
                val units = if (defaultUnits == Uom.G) Uom.KG else defaultUnits
                val ean = getEanByMaterialUnits(material, defaultUnits.code)
                val shelfLifeTypes = getShelfLifeTypes()
                val comments = getWorkListComments()

                return@withContext Good(
                        ean = ean,
                        material = material,
                        name = goodInfo.name,
                        defaultUnits = defaultUnits,
                        units = units,
                        goodGroup = goodInfo.goodGroup,
                        purchaseGroup = goodInfo.purchaseGroup,
                        shelfLife = goodInfo.shelfLife,
                        remainingShelfLife = goodInfo.remainingShelfLife,
                        shelfLifeTypes = shelfLifeTypes,
                        comments = comments,
                        options = GoodOptions(
                                matrixType = getMatrixType(goodInfo.matrixType),
                                section = if (goodInfo.section.isNotEmpty()) goodInfo.section else "91",
                                goodType = getGoodType(
                                        alcohol = goodInfo.isAlcohol,
                                        excise = goodInfo.isExcise,
                                        marked = goodInfo.isMark,
                                        rusWine = goodInfo.isRusWine.orEmpty()
                                ),
                                healthFood = goodInfo.healthFood.isSapTrue(),
                                novelty = goodInfo.novelty.isSapTrue()
                        )
                )
            }

            return@withContext null
        }
    }

    override suspend fun getGoodByEan(ean: String): Good? {
        val scanCodeInfo = ScanCodeInfo(ean)

        return withContext(Dispatchers.IO) {
            getMaterialByEan(scanCodeInfo.eanWithoutWeight)?.let { eanInfo ->
                getGoodInfoByMaterial(eanInfo.materialNumber)?.let { goodInfo ->
                    val defaultUnits = Uom(code = goodInfo.unitsCode, name = getUnitsName(goodInfo.unitsCode))
                    val units = if (defaultUnits == Uom.G) Uom.KG else defaultUnits
                    val shelfLifeTypes = getShelfLifeTypes()
                    val comments = getWorkListComments()
                    val quantity = scanCodeInfo.getQuantity(defaultUnits)

                    return@withContext Good(
                            ean = eanInfo.ean,
                            material = eanInfo.materialNumber,
                            name = goodInfo.name,
                            defaultUnits = defaultUnits,
                            units = units,
                            goodGroup = goodInfo.goodGroup,
                            purchaseGroup = goodInfo.purchaseGroup,
                            shelfLife = goodInfo.shelfLife,
                            remainingShelfLife = goodInfo.remainingShelfLife,
                            shelfLifeTypes = shelfLifeTypes,
                            comments = comments,
                            defaultValue = quantity,
                            options = GoodOptions(
                                    matrixType = getMatrixType(goodInfo.matrixType),
                                    section = if (goodInfo.section.isNotEmpty()) goodInfo.section else "91",
                                    goodType = getGoodType(
                                            alcohol = goodInfo.isAlcohol,
                                            excise = goodInfo.isExcise,
                                            marked = goodInfo.isMark,
                                            rusWine = goodInfo.isRusWine.orEmpty()
                                    ),
                                    healthFood = goodInfo.healthFood.isSapTrue(),
                                    novelty = goodInfo.novelty.isSapTrue()
                            )
                    )
                }
            }

            return@withContext null
        }
    }

    private suspend fun getGoodInfoByMaterial(material: String?): WorkListGoodInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext productInfo.getProductInfoByMaterial(material)?.toWorkListGoodInfo()
        }
    }

    private suspend fun getMaterialByEan(ean: String?): EanInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext eanInfo.getEanInfo(ean)?.toEanInfo()
        }
    }

    private suspend fun getEanByMaterialUnits(material: String, unitsCode: String): String? {
        return withContext(Dispatchers.IO) {
            return@withContext eanInfo.getEanInfoByMaterialUnits(material, unitsCode)?.toEanInfo()?.ean
        }
    }

    private suspend fun getShelfLifeTypes(): List<DictElement> {
        return withContext(Dispatchers.IO) {
            val selfLives = dictonary.getItemsByTid("007")?.toElementList()?.toMutableList()
                    ?: mutableListOf() // 007 - Типы сроков годности
            selfLives.sortBy { it.order }
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
            return@withContext comments
        }
    }

    private suspend fun getUnitsName(code: String?): String {
        return withContext(Dispatchers.IO) {
            return@withContext units.getUnitName(code)?.toLowerCase(Locale.getDefault()).orEmpty()
        }
    }
}

interface IWorkListRepo {
    suspend fun getGoodByMaterial(material: String): Good?
    suspend fun getGoodByEan(ean: String): Good?
}