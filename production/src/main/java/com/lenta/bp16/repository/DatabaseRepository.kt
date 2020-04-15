package com.lenta.bp16.repository

import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz17V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
        private val hyperHive: HyperHive
) : IDatabaseRepository {

    private val units: ZmpUtz07V001 by lazy { ZmpUtz07V001(hyperHive) } // Единицы измерения
    private val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки
    private val dictonary: ZmpUtz17V001 by lazy { ZmpUtz17V001(hyperHive) } // Справочник с наборами данных

    override suspend fun getAllowedAppVersion(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getAllowedProAppVersion()
        }
    }

    override suspend fun getUnitsByCode(code: String): Uom {
        return withContext(Dispatchers.IO) {
            return@withContext units.getUnitName(code)?.toLowerCase(Locale.getDefault())?.let { name ->
                Uom(code, name)
            } ?: Uom.KG
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

    override suspend fun getLabelLimit(): Int {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getLabelLimit()?.toIntOrNull() ?: 0
        }
    }

    override suspend fun getCategoryList(): List<DictElement> {
        return withContext(Dispatchers.IO) {
            val list = dictonary.getItemsByTidSorted("025")?.toElementList()?.toMutableList() ?: mutableListOf()
            if (list.size > 1) {
                list.add(0, DictElement(
                        code = "0",
                        order = "0",
                        description = ""
                ))
            }

            return@withContext list
        }
    }

    override suspend fun getDefectList(): List<DictElement> {
        return withContext(Dispatchers.IO) {
            val list = dictonary.getItemsByTidSorted("024")?.toElementList()?.toMutableList() ?: mutableListOf()
            if (list.size > 1) {
                list.add(0, DictElement(
                        code = "0",
                        order = "0",
                        description = ""
                ))
            }

            return@withContext list
        }
    }

    override suspend fun getCategory(categoryCode: String): DictElement? {
        return withContext(Dispatchers.IO) {
            return@withContext getCategoryList().find { it.code == categoryCode }
        }
    }

    override suspend fun getDefect(defectCode: String): DictElement? {
        return withContext(Dispatchers.IO) {
            var formattedCode = defectCode
            while (formattedCode.startsWith("0")) {
                formattedCode = formattedCode.substring(1)
            }
            return@withContext getDefectList().find { it.code == formattedCode }
        }
    }

}

interface IDatabaseRepository {

    suspend fun getAllowedAppVersion(): String?
    suspend fun getUnitsByCode(code: String): Uom

    suspend fun getServerAddress(): String?
    suspend fun getPcpContTimeMm(): Int
    suspend fun getPcpExpirTimeMm(): Int
    suspend fun getLabelLimit(): Int
    suspend fun getCategoryList(): List<DictElement>
    suspend fun getDefectList(): List<DictElement>
    suspend fun getCategory(categoryCode: String): DictElement?
    suspend fun getDefect(defectCode: String): DictElement?

}