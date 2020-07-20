package com.lenta.bp18.repository

import com.lenta.bp18.model.pojo.EnteredCode
import com.lenta.bp18.model.pojo.Good
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

    override suspend fun getGoodInfoByEan(ean: String): Good? {
        return withContext(Dispatchers.IO) {
            getEanInfoByEan(ean)?.run {
                val productInfo = getProductInfoByMaterial(this.materialNumber)
                val unitName = getGoodUnitName(productInfo?.buom)
                return@withContext Good(
                        ean = ean,
                        material = this.materialNumber,
                        matcode = productInfo?.matcode.orEmpty(),
                        enteredCode = EnteredCode.EAN,
                        name = productInfo?.name.orEmpty(),
                        uom = Uom(
                                code = productInfo?.buom.orEmpty(),
                                name = unitName.orEmpty()))
            }
        }
    }

    override suspend fun getGoodInfoByMaterial(material: String): Good? {
        return withContext(Dispatchers.IO) {
            getProductInfoByMaterial(material)?.run {
                val eanInfo = getEanInfoByMaterial(this.material)
                val unitName = getGoodUnitName(this.buom)
                return@withContext Good(
                        ean = eanInfo?.ean.orEmpty(),
                        material = material,
                        matcode = this.matcode,
                        enteredCode = EnteredCode.MATERIAL,
                        name = this.name,
                        uom = Uom(
                                code = this.buom,
                                name = unitName.orEmpty()))
            }

        }
    }

    override suspend fun getGoodInfoByMatcode(matcode: String): Good? {
        return withContext(Dispatchers.IO) {
            getProductInfoByMatcode(matcode)?.run {
                val eanInfo = getEanInfoByMaterial(this.material)
                val unitName = getGoodUnitName(this.buom)
                return@withContext Good(
                        ean = eanInfo?.ean.orEmpty(),
                        material = this.material,
                        matcode = matcode,
                        enteredCode = EnteredCode.MATCODE,
                        name = this.name,
                        uom = Uom(
                                code = this.buom,
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
    suspend fun getGoodInfoByEan(ean: String): Good?
    suspend fun getGoodInfoByMaterial(material: String): Good?
    suspend fun getGoodInfoByMatcode(matcode: String): Good?
    suspend fun getAllMarkets(): List<MarketInfo>
}