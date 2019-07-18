package com.lenta.bp7.repos

import com.lenta.bp7.data.Enabled
import com.lenta.bp7.data.StoreRetailType
import com.lenta.shared.di.AppScope
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz23V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz24V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.requests.combined.scan_info.pojo.EanInfo
import com.lenta.shared.requests.combined.scan_info.pojo.GoodInfo
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AppScope
class DatabaseRepo(hyperHive: HyperHive) : IDatabaseRepo {

    private val units: ZmpUtz07V001 by lazy { ZmpUtz07V001(hyperHive) } // Единицы измерения
    private val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки
    private val stores: ZmpUtz23V001 by lazy { ZmpUtz23V001(hyperHive) } // Список магазинов
    private val goodInfo: ZmpUtz24V001 by lazy { ZmpUtz24V001(hyperHive) } // Информация о товаре
    private val barCodeInfo: ZmpUtz25V001 by lazy { ZmpUtz25V001(hyperHive) } // Информация о штрих-коде

    override suspend fun getBarCodeInfoByBarCode(barCode: String): EanInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext barCodeInfo.getEanInfo(barCode)?.toEanInfo()
        }
    }

    override suspend fun getBarCodeInfoBySapCode(sapCode: String): EanInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext barCodeInfo.getEanInfoFromMaterial(sapCode)?.toEanInfo()
        }
    }

    override suspend fun getGoodInfo(sapCode: String): GoodInfo? {
        return withContext(Dispatchers.IO) {
            return@withContext goodInfo.getGoodInfo(sapCode)?.toGoodInfo()
        }
    }

    override suspend fun getGoodUnit(unitCode: String): String? {
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
}

interface IDatabaseRepo {
    suspend fun getRetailType(marketNumber: String?): String?
    suspend fun getFacingsParam(marketNumber: String?): String?
    suspend fun getPlacesParam(marketNumber: String?): String?
    suspend fun getSelfControlPinCode(): String?
    suspend fun getExternalAuditPinCode(): String?
    suspend fun getBarCodeInfoByBarCode(barCode: String): EanInfo?
    suspend fun getBarCodeInfoBySapCode(sapCode: String): EanInfo?
    suspend fun getGoodInfo(sapCode: String): GoodInfo?
    suspend fun getGoodUnit(unitCode: String): String?
}