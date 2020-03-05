package com.lenta.bp12.repository

import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.*
import com.lenta.shared.fmp.resources.slow.*
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
        private val hyperHive: HyperHive
) : IDatabaseRepository {

    private val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки
    private val units: ZmpUtz07V001 by lazy { ZmpUtz07V001(hyperHive) } // Единицы измерения
    private val valueLists: ZmpUtz17V001 by lazy { ZmpUtz17V001(hyperHive) } // Списки значений
    private val printers: ZmpUtz26V001 by lazy { ZmpUtz26V001(hyperHive) } // Принтеры
    private val iconDescriptions: ZmpUtz38V001 by lazy { ZmpUtz38V001(hyperHive) } // Описание иконок
    private val taskTypes: ZmpUtz39V001 by lazy { ZmpUtz39V001(hyperHive) } // Типы заданий
    private val storages: ZmpUtz40V001 by lazy { ZmpUtz40V001(hyperHive) } // Склады
    private val allowed: ZmpUtz41V001 by lazy { ZmpUtz41V001(hyperHive) } // Разрешенные товары
    private val forbidden: ZmpUtz42V001 by lazy { ZmpUtz42V001(hyperHive) } // Запрещенные товары
    private val returnReasons: ZmpUtz44V001 by lazy { ZmpUtz44V001(hyperHive) } // Причины возврата
    private val suppliers: ZmpUtz09V001 by lazy { ZmpUtz09V001(hyperHive) } // Поставщики
    private val alcohol: ZmpUtz22V001 by lazy { ZmpUtz22V001(hyperHive) } // Алкогольные товары
    private val defaultUnits: ZmpUtz25V001 by lazy { ZmpUtz25V001(hyperHive) } // Единицы измерения по умолчанию
    private val goods: ZmpUtz30V001 by lazy { ZmpUtz30V001(hyperHive) } // Товары
    private val producers: ZmpUtz43V001 by lazy { ZmpUtz43V001(hyperHive) } // Производители


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

    override suspend fun getTaskTypeList(): List<TaskType> {
        return withContext(Dispatchers.IO) {
            return@withContext taskTypes.getTaskTypeList()
        }
    }

    override suspend fun getStorageList(taskType: String): List<String> {
        return withContext(Dispatchers.IO) {
            return@withContext storages.getStorageList(taskType)
        }
    }

    override suspend fun getReturnReasonList(taskType: String): List<ReturnReason> {
        return withContext(Dispatchers.IO) {
            return@withContext returnReasons.getReturnReasonList(taskType)
        }
    }
}

interface IDatabaseRepository {

    suspend fun getAllowedAppVersion(): String?
    suspend fun getUnitsByCode(code: String): Uom
    suspend fun getTaskTypeList(): List<TaskType>
    suspend fun getStorageList(taskType: String): List<String>
    suspend fun getReturnReasonList(taskType: String): List<ReturnReason>

}