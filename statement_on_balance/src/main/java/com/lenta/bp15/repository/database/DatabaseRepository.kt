package com.lenta.bp15.repository.database


import com.lenta.bp15.platform.extention.getTaskTypeByCode
import com.lenta.bp15.platform.extention.getTaskTypeList
import com.lenta.bp15.platform.extention.getUnknownTaskType
import com.lenta.bp15.repository.database.pojo.TaskType
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.*
import com.lenta.shared.fmp.resources.slow.*
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
        private val hyperHive: HyperHive
) : IDatabaseRepository {

    private val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки
    private val icons: ZmpUtz38V001 by lazy { ZmpUtz38V001(hyperHive) } // Описание иконок
    private val taskTypes: ZfmpUtz52V001 by lazy { ZfmpUtz52V001(hyperHive) } // Типы заданий
    private val markTypes: ZfmpUtz53V001 by lazy { ZfmpUtz53V001(hyperHive) } // Типы марок

    //private val eanInfo: ZmpUtz25V001 by lazy { ZmpUtz25V001(hyperHive) } // Информация о штрих-коде
    private val products: ZfmpUtz48V001 by lazy { ZfmpUtz48V001(hyperHive) } // Информация о товаре

    /*private val dictionary: ZmpUtz17V001 by lazy { ZmpUtz17V001(hyperHive) } // Справочник с наборами данных
    private val units: ZmpUtz07V001 by lazy { ZmpUtz07V001(hyperHive) } // Единицы измерения
    private val printers: ZmpUtz26V001 by lazy { ZmpUtz26V001(hyperHive) } // Принтеры
    private val taskTypes: ZmpUtz39V001 by lazy { ZmpUtz39V001(hyperHive) } // Типы заданий
    private val storages: ZmpUtz40V001 by lazy { ZmpUtz40V001(hyperHive) } // Склады
    private val allowed: ZmpUtz41V001 by lazy { ZmpUtz41V001(hyperHive) } // Разрешенные товары
    private val forbidden: ZmpUtz42V001 by lazy { ZmpUtz42V001(hyperHive) } // Запрещенные товары
    private val returnReasons: ZmpUtz44V001 by lazy { ZmpUtz44V001(hyperHive) } // Причины возврата
    private val providers: ZmpUtz09V001 by lazy { ZmpUtz09V001(hyperHive) } // Поставщики
    private val alcoCodes: ZmpUtz22V001 by lazy { ZmpUtz22V001(hyperHive) } // Алкокоды
    private val goods: ZmpUtz30V001 by lazy { ZmpUtz30V001(hyperHive) } // Товары
    private val producers: ZmpUtz43V001 by lazy { ZmpUtz43V001(hyperHive) } // Производители
    private val markGroup: ZmpUtz109V001 by lazy { ZmpUtz109V001(hyperHive) } //группы маркировки*/


    /*private suspend fun getEanByMaterialUnits(material: String, unitsCode: String): String {
        return withContext(Dispatchers.IO) {
            eanInfo.getEanInfoByMaterialUnits(material, unitsCode)?.toEanInfo()?.ean.orEmpty()
        }
    }

    override suspend fun getEanListByMaterialUnits(material: String, unitsCode: String): List<String> {
        return withContext(Dispatchers.IO) {
            eanInfo.getEanListByMaterialUnits(material, unitsCode)
        }
    }

    override suspend fun getEanInfo(ean: String): com.lenta.shared.requests.combined.scan_info.pojo.EanInfo? {
        return eanInfo.getEanInfo(ean)?.toEanInfo()
    }*/

    override suspend fun getAllowedAppVersion(): String? {
        return withContext(Dispatchers.IO) {
            settings.getAllowedSobAppVersion()
        }
    }

    override suspend fun getTaskTypeList(): List<TaskType> {
        return withContext(Dispatchers.IO) {
            taskTypes.getTaskTypeList()
        }
    }

    override suspend fun getTaskTypeByCode(taskTypeCode: String): TaskType {
        return withContext(Dispatchers.IO) {
            taskTypes.getTaskTypeByCode(taskTypeCode) ?: getUnknownTaskType()
        }
    }
}

interface IDatabaseRepository {

    //suspend fun getEanListByMaterialUnits(material: String, unitsCode: String): List<String>
    //suspend fun getEanInfo(ean: String): com.lenta.shared.requests.combined.scan_info.pojo.EanInfo?
    suspend fun getAllowedAppVersion(): String?
    suspend fun getTaskTypeList(): List<TaskType>
    suspend fun getTaskTypeByCode(taskTypeCode: String): TaskType

}