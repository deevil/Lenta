package com.lenta.bp12.repository

import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class GeneralRepository @Inject constructor(
        private val hyperHive: HyperHive
) : IGeneralRepository {

    private val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки
    private val units: ZmpUtz07V001 by lazy { ZmpUtz07V001(hyperHive) } // Единицы измерения

    override suspend fun getAllowedAppVersion(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getAllowedBksAppVersion()
        }
    }

    override suspend fun getUnitsByCode(code: String): Uom {
        return withContext(Dispatchers.IO) {
            return@withContext units.getUnitName(code)?.toLowerCase(Locale.getDefault())?.let { name ->
                Uom(code, name)
            } ?: Uom.KG
        }
    }

}

interface IGeneralRepository {

    suspend fun getAllowedAppVersion(): String?
    suspend fun getUnitsByCode(code: String): Uom

}