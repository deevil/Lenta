package com.lenta.bp10.repos

import com.lenta.bp10.fmp.resources.dao_ext.isChkOwnpr
import com.lenta.bp10.fmp.resources.tasks_settings.ZmpUtz29V001Rfc
import com.lenta.shared.fmp.resources.dao_ext.getSpecialTaskTypes
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
        private val hyperHive: HyperHive
) : IDatabaseRepository {

    private val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки
    private val zmpUtz29V001: ZmpUtz29V001Rfc by lazy { ZmpUtz29V001Rfc(hyperHive) } // Типы заданий???


    override suspend fun isChkOwnpr(taskTypeCode: String): Boolean {
        return withContext(Dispatchers.IO) {
            zmpUtz29V001.isChkOwnpr(taskTypeCode)
        }
    }

    override suspend fun isSpecialMode(taskTypeCode: String): Boolean {
        return withContext(Dispatchers.IO) {
            val specialTaskTypes = settings.getSpecialTaskTypes()
            Logg.d { "--> Special task types: $specialTaskTypes" }
            specialTaskTypes.contains(taskTypeCode)
        }
    }

}

interface IDatabaseRepository {

    suspend fun isChkOwnpr(taskTypeCode: String): Boolean
    suspend fun isSpecialMode(taskTypeCode: String): Boolean

}