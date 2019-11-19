package com.lenta.bp16.repository

import com.lenta.shared.fmp.resources.dao_ext.getAllowedProAppVersion
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GeneralRepository @Inject constructor(
        private val hyperHive: HyperHive
) : IGeneralRepository {

    private val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки

    override suspend fun getAllowedAppVersion(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getAllowedProAppVersion()
        }
    }

}

interface IGeneralRepository {

    suspend fun getAllowedAppVersion(): String?

}