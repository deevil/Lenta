package com.lenta.bp7.repos

import com.lenta.bp7.data.StoreRetailType
import com.lenta.bp7.data.Enabled
import com.lenta.shared.di.AppScope
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz23V001
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AppScope
class DatabaseRepo(hyperHive: HyperHive) : IDatabaseRepo {

    private val zmpUtz14V001: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки
    private val zmpUtz23V001: ZmpUtz23V001 by lazy { ZmpUtz23V001(hyperHive) } // Список магазинов

    override suspend fun getRetailType(marketNumber: String?): String? {
        return withContext(Dispatchers.IO) {
            return@withContext marketNumber?.let { zmpUtz23V001.getRetailType(it) }
        }
    }

    override suspend fun getFacingsParam(marketNumber: String?): String? {
        return withContext(Dispatchers.IO) {
            when (getRetailType(marketNumber)) {
                StoreRetailType.HYPER.type -> return@withContext zmpUtz14V001.getFacingsHyperParam()
                StoreRetailType.SUPER.type -> return@withContext zmpUtz14V001.getFacingsSuperParam()
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
                StoreRetailType.HYPER.type -> return@withContext zmpUtz14V001.getPlacesHyperParam()
                StoreRetailType.SUPER.type -> return@withContext zmpUtz14V001.getFacingsSuperParam()
                else -> {
                    Logg.d { "Store retail type unknown!" }
                    return@withContext Enabled.NO.type
                }
            }
        }
    }

    override suspend fun getSelfControlPinCode(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext zmpUtz14V001.getSelfControlPinCode()
        }
    }

    override suspend fun getExternalAuditPinCode(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext zmpUtz14V001.getExternalAuditPinCode()
        }
    }
}

interface IDatabaseRepo {
    suspend fun getRetailType(marketNumber: String?): String?
    suspend fun getFacingsParam(marketNumber: String?): String?
    suspend fun getPlacesParam(marketNumber: String?): String?
    suspend fun getSelfControlPinCode(): String?
    suspend fun getExternalAuditPinCode(): String?
}