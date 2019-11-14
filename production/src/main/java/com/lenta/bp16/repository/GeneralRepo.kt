package com.lenta.bp16.repository

import com.lenta.shared.fmp.resources.dao_ext.getAllowedWklAppVersion
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GeneralRepo @Inject constructor(
        private val hyperHive: HyperHive
) : IGeneralRepo {

    private val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки

    override suspend fun getAllowedAppVersion(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getAllowedWklAppVersion()
        }
    }

}


interface IGeneralRepo {

    suspend fun getAllowedAppVersion(): String?

}