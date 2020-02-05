package com.lenta.bp16.repo

import com.lenta.shared.fmp.resources.dao_ext.getAllowedPleAppVersion
import com.lenta.shared.fmp.resources.dao_ext.getPcpContTimeMm
import com.lenta.shared.fmp.resources.dao_ext.getPcpExpirTimeMm
import com.lenta.shared.fmp.resources.dao_ext.getServerAddress
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz23V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseRepository(
        hyperHive: HyperHive,
        val units: ZmpUtz07V001 = ZmpUtz07V001(hyperHive), // Единицы измерения
        val settings: ZmpUtz14V001 = ZmpUtz14V001(hyperHive), // Настройки
        val stores: ZmpUtz23V001 = ZmpUtz23V001(hyperHive), // Список магазинов
        val productInfo: ZfmpUtz48V001 = ZfmpUtz48V001(hyperHive), // Информация о товаре
        val barCodeInfo: ZmpUtz25V001 = ZmpUtz25V001(hyperHive) // Информация о штрих-коде
) : IDatabaseRepository {

    override suspend fun getAllowedAppVersion(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getAllowedPleAppVersion() // todo Поправить, когда добавят соответствующий параметр
        }
    }

    override suspend fun getServerAddress(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getServerAddress()
        }
    }

    override suspend fun getPcpContTimeMm(): Int {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getPcpContTimeMm()?.toIntOrNull() ?: 0
        }
    }

    override suspend fun getPcpExpirTimeMm(): Int {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getPcpExpirTimeMm()?.toIntOrNull() ?: 0
        }
    }

}

interface IDatabaseRepository {
    suspend fun getAllowedAppVersion(): String?
    suspend fun getServerAddress(): String?
    suspend fun getPcpContTimeMm(): Int
    suspend fun getPcpExpirTimeMm(): Int
}