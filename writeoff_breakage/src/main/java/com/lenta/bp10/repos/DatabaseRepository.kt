package com.lenta.bp10.repos

import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz46V001
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.pojo.EanInfo
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
        private val hyperHive: HyperHive,
        private val repoInMemoryHolder: IRepoInMemoryHolder
) : IDatabaseRepository {

    private val eanInfo: ZmpUtz25V001 by lazy { ZmpUtz25V001(hyperHive) } // Информация о штрих-коде
    private val products: ZfmpUtz48V001 by lazy { ZfmpUtz48V001(hyperHive) } // Информация о товаре
    private val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки
    private val units: ZmpUtz07V001 by lazy { ZmpUtz07V001(hyperHive) } // Единицы измерения

    private val setInfo: ZmpUtz46V001 by lazy { ZmpUtz46V001(hyperHive) } // Информация о наборах


    override suspend fun getEanByMaterialUnits(material: String, unitsCode: String): String {
        return withContext(Dispatchers.IO) {
            eanInfo.getEanInfoByMaterialUnits(material, unitsCode)?.toEanInfo()?.ean.orEmpty()
        }
    }

    // Типы заданий для списания
    override suspend fun isChkOwnpr(taskTypeCode: String): Boolean {
        return withContext(Dispatchers.IO) {
            val allResults = repoInMemoryHolder.userResourceResult?.taskSettings.orEmpty()
            val result = allResults.filter { it.taskType == taskTypeCode && it.chkOwnpr == "X" }
            result.isNotEmpty()
        }
    }

    override suspend fun isSpecialMode(taskTypeCode: String): Boolean {
        return withContext(Dispatchers.IO) {
            val specialTaskTypes = settings.getSpecialTaskTypes()
            Logg.d { "--> Special task types: $specialTaskTypes" }
            specialTaskTypes.contains(taskTypeCode)
        }
    }

    override suspend fun getProductInfoByMaterial(material: String): ProductInfo? {
        return withContext(Dispatchers.IO) {
            products.getProductInfoByMaterial(material)?.let { materialInfo ->
                with(materialInfo) {
                    ProductInfo(
                            materialNumber = material,
                            description = name.orEmpty(),
                            uom = getUnitsByCode(buom.orEmpty()),
                            type = getProductType(),
                            isSet = setInfo.isSet(material),
                            sectionId = abtnr.orEmpty(),
                            matrixType = getMatrixType(),
                            materialType = matype.orEmpty(),
                            markedGoodType = markType.orEmpty()
                    )
                }
            }
        }
    }

    override suspend fun getProductInfoByEan(ean: String): ProductInfo? {
        return withContext(Dispatchers.IO) {
            getEanInfo(ean)?.let { eanInfo ->
                getProductInfoByMaterial(eanInfo.materialNumber)
            }
        }
    }

    private fun getEanInfo(ean: String): EanInfo? {
        return eanInfo.getEanInfo(ean)?.toEanInfo()
    }

    override suspend fun getUnitsByCode(code: String): Uom {
        return withContext(Dispatchers.IO) {
            units.getUnitName(code)?.let { name ->
                Uom(code, name)
            } ?: Uom.ST
        }
    }
}

interface IDatabaseRepository {

    suspend fun getEanByMaterialUnits(material: String, unitsCode: String): String
    suspend fun isChkOwnpr(taskTypeCode: String): Boolean
    suspend fun isSpecialMode(taskTypeCode: String): Boolean
    suspend fun getUnitsByCode(code: String): Uom

    suspend fun getProductInfoByMaterial(material: String): ProductInfo?
    suspend fun getProductInfoByEan(ean: String): ProductInfo?

}