package com.lenta.bp7.repos

import com.lenta.bp7.data.Enabled
import com.lenta.bp7.data.SapCodeType
import com.lenta.bp7.data.StoreRetailType
import com.lenta.bp7.data.model.GoodInfo
import com.lenta.shared.di.AppScope
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz23V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.requests.combined.scan_info.pojo.EanInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ProductInfo
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AppScope
class DatabaseRepo(hyperHive: HyperHive) : IDatabaseRepo {

    private val units: ZmpUtz07V001 by lazy { ZmpUtz07V001(hyperHive) } // Единицы измерения
    private val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки
    private val stores: ZmpUtz23V001 by lazy { ZmpUtz23V001(hyperHive) } // Список магазинов
    private val goodInfo: ZfmpUtz48V001 by lazy { ZfmpUtz48V001(hyperHive) } // Информация о товаре
    private val barCodeInfo: ZmpUtz25V001 by lazy { ZmpUtz25V001(hyperHive) } // Информация о штрих-коде

    override suspend fun getGoodInfoByBarCode(barCode: String): GoodInfo? {
        return withContext(Dispatchers.IO) {
            val eanInfo = getEanInfoByBarCode(barCode)
            if (eanInfo == null) {
                return@withContext null
            } else {
                val materialInfo = getProductInfoByMaterial(eanInfo.materialNumber)
                val unitName = getGoodUnitName(materialInfo?.buom)
                return@withContext GoodInfo(
                        sapCode = eanInfo.materialNumber,
                        barCode = barCode,
                        name = materialInfo?.name ?: "Not found!",
                        unitsCode = materialInfo?.buom ?: "Not found!",
                        units = unitName ?: "Not found!")
            }
        }
    }

    override suspend fun getGoodInfoBySapCode(sapCode: String, type: SapCodeType): GoodInfo? {
        return withContext(Dispatchers.IO) {
            val productInfo = when (type) {
                SapCodeType.MATERIAL -> getProductInfoByMaterial(sapCode)
                SapCodeType.MATCODE -> getProductInfoByMatcode(sapCode)
            }
            val eanInfo = getEanInfoBySapCode(productInfo?.material)
            if (eanInfo == null) {
                return@withContext null
            } else {
                val unitName = getGoodUnitName(productInfo?.buom)
                return@withContext GoodInfo(
                        sapCode = sapCode,
                        barCode = eanInfo.ean,
                        name = productInfo?.name ?: "Not found!",
                        unitsCode = productInfo?.buom ?: "Not found!",
                        units = unitName ?: "Not found!")
            }
        }
    }

    override suspend fun getEanInfoByBarCode(barCode: String?): EanInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext barCodeInfo.getEanInfo(barCode)?.toEanInfo()
        }
    }

    override suspend fun getEanInfoBySapCode(sapCode: String?): EanInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext barCodeInfo.getEanInfoFromMaterial(sapCode)?.toEanInfo()
        }
    }

    override suspend fun getProductInfoByMaterial(material: String?): ProductInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext goodInfo.getProductInfoByMaterial(material)?.toMaterialInfo()
        }
    }

    override suspend fun getProductInfoByMatcode(matcode: String?): ProductInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext goodInfo.getProductInfoByMatcode(matcode)?.toMaterialInfo()
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

    override suspend fun getFacingsParam(marketNumber: String?): String? {
        return withContext(Dispatchers.IO) {
            when (getRetailType(marketNumber)) {
                StoreRetailType.HYPER.type -> return@withContext settings.getFacingsHyperParam()
                StoreRetailType.SUPER.type -> return@withContext settings.getFacingsSuperParam()
                else -> {
                    Logg.d { "Store retail type unknown!" }
                    return@withContext Enabled.NO.type
                }
            }
        }
    }

    override suspend fun getPlacesParam(marketNumber: String?): String? {
        return withContext(Dispatchers.IO) {
            when (getRetailType(marketNumber)) {
                StoreRetailType.HYPER.type -> return@withContext settings.getPlacesHyperParam()
                StoreRetailType.SUPER.type -> return@withContext settings.getFacingsSuperParam()
                else -> {
                    Logg.d { "Store retail type unknown!" }
                    return@withContext Enabled.NO.type
                }
            }
        }
    }

    override suspend fun getSelfControlPinCode(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getSelfControlPinCode()
        }
    }

    override suspend fun getExternalAuditPinCode(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getExternalAuditPinCode()
        }
    }

    override suspend fun getAllowedAppVersion(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getAllowedPleAppVersion()
        }
    }
}

interface IDatabaseRepo {
    suspend fun getRetailType(marketNumber: String?): String?
    suspend fun getFacingsParam(marketNumber: String?): String?
    suspend fun getPlacesParam(marketNumber: String?): String?
    suspend fun getSelfControlPinCode(): String?
    suspend fun getExternalAuditPinCode(): String?
    suspend fun getEanInfoByBarCode(barCode: String?): EanInfo?
    suspend fun getEanInfoBySapCode(sapCode: String?): EanInfo?
    suspend fun getProductInfoByMaterial(material: String?): ProductInfo?
    suspend fun getProductInfoByMatcode(matcode: String?): ProductInfo?
    suspend fun getGoodUnitName(unitCode: String?): String?
    suspend fun getGoodInfoByBarCode(barCode: String): GoodInfo?
    suspend fun getGoodInfoBySapCode(sapCode: String, type: SapCodeType): GoodInfo?
    suspend fun getAllowedAppVersion(): String?
}