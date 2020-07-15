package com.lenta.bp18.repository

import com.lenta.bp18.data.model.EnteredCode
import com.lenta.bp18.data.model.GoodInfo
import com.lenta.shared.di.AppScope
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz23V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.pojo.MarketInfo
import com.lenta.shared.requests.combined.scan_info.pojo.EanInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ProductInfo
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AppScope
class DatabaseRepo(
        hyperHive: HyperHive,
        val units: ZmpUtz07V001 = ZmpUtz07V001(hyperHive), // Единицы измерения
        val settings: ZmpUtz14V001 = ZmpUtz14V001(hyperHive), // Настройки
        val stores: ZmpUtz23V001 = ZmpUtz23V001(hyperHive), // Список магазинов
        val productInfo: ZfmpUtz48V001 = ZfmpUtz48V001(hyperHive), // Информация о товаре
        val barCodeInfo: ZmpUtz25V001 = ZmpUtz25V001(hyperHive) // Информация о штрих-коде
) : IDatabaseRepo {

    override suspend fun getGoodInfoByEan(ean: String): GoodInfo? {
        return withContext(Dispatchers.IO) {
            val eanInfo = getEanInfoByEan(ean)
            if (eanInfo == null) {
                return@withContext null
            } else {
                val productInfo = getProductInfoByMaterial(eanInfo.materialNumber)
                val unitName = getGoodUnitName(productInfo?.buom)
                return@withContext GoodInfo(
                        ean = ean,
                        material = eanInfo.materialNumber,
                        matcode = productInfo?.matcode.orEmpty(),
                        enteredCode = EnteredCode.EAN,
                        name = productInfo?.name.orEmpty(),
                        uom = Uom(
                                code = productInfo?.buom.orEmpty(),
                                name = unitName.orEmpty()))
            }
        }
    }

    override suspend fun getGoodInfoByMaterial(material: String): GoodInfo? {
        return withContext(Dispatchers.IO) {
            val productInfo = getProductInfoByMaterial(material)
            if (productInfo == null) {
                return@withContext null
            } else {
                val eanInfo = getEanInfoByMaterial(productInfo.material)
                val unitName = getGoodUnitName(productInfo.buom)
                return@withContext GoodInfo(
                        ean = eanInfo?.ean.orEmpty(),
                        material = material,
                        matcode = productInfo.matcode,
                        enteredCode = EnteredCode.MATERIAL,
                        name = productInfo.name,
                        uom = Uom(
                                code = productInfo.buom,
                                name = unitName.orEmpty()))
            }
        }
    }

    override suspend fun getGoodInfoByMatcode(matcode: String): GoodInfo? {
        return withContext(Dispatchers.IO) {
            val productInfo = getProductInfoByMatcode(matcode)
            if (productInfo == null) {
                return@withContext null
            } else {
                val eanInfo = getEanInfoByMaterial(productInfo.material)
                val unitName = getGoodUnitName(productInfo.buom)
                return@withContext GoodInfo(
                        ean = eanInfo?.ean.orEmpty(),
                        material = productInfo.material,
                        matcode = matcode,
                        enteredCode = EnteredCode.MATCODE,
                        name = productInfo.name,
                        uom = Uom(
                                code = productInfo.buom,
                                name = unitName.orEmpty()))
            }
        }
    }

    override suspend fun getEanInfoByEan(ean: String?): EanInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext barCodeInfo.getEanInfo(ean)?.toEanInfo()
        }
    }

    override suspend fun getEanInfoByMaterial(material: String?): EanInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext barCodeInfo.getEanInfoFromMaterial(material)?.toEanInfo()
        }
    }

    override suspend fun getProductInfoByMaterial(material: String?): ProductInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext productInfo.getProductInfoByMaterial(material)?.toMaterialInfo()
        }
    }

    override suspend fun getProductInfoByMatcode(matcode: String?): ProductInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext productInfo.getProductInfoByMatcode(matcode)?.toMaterialInfo()
        }
    }

    override suspend fun getGoodUnitName(unitCode: String?): String? {
        return withContext(Dispatchers.IO) {
            return@withContext units.getUnitName(unitCode)
        }
    }

    override suspend fun getRetailType(marketNumber: String?): String? {
        return withContext(Dispatchers.IO) {
            return@withContext marketNumber?.let { stores.getRetailType(it) }
        }
    }


    override suspend fun getAllMarkets(): List<MarketInfo> {
        return withContext(Dispatchers.IO) {
            return@withContext stores.getAllMarkets().toMarketInfoList()
        }
    }

}

interface IDatabaseRepo {
    suspend fun getRetailType(marketNumber: String?): String?
    suspend fun getEanInfoByEan(ean: String?): EanInfo?
    suspend fun getEanInfoByMaterial(material: String?): EanInfo?
    suspend fun getProductInfoByMaterial(material: String?): ProductInfo?
    suspend fun getProductInfoByMatcode(matcode: String?): ProductInfo?
    suspend fun getGoodUnitName(unitCode: String?): String?
    suspend fun getGoodInfoByEan(ean: String): GoodInfo?
    suspend fun getGoodInfoByMaterial(material: String): GoodInfo?
    suspend fun getGoodInfoByMatcode(matcode: String): GoodInfo?
    suspend fun getAllMarkets(): List<MarketInfo>
}