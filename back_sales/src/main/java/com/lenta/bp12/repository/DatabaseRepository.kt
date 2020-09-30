package com.lenta.bp12.repository

import com.lenta.bp12.model.MarkType
import com.lenta.bp12.model.pojo.AlcoCodeInfo
import com.lenta.bp12.model.pojo.MarkTypeGroup
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.platform.extention.*
import com.lenta.bp12.request.pojo.GoodInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.*
import com.lenta.shared.fmp.resources.slow.*
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.enumValueOrNull
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.orIfNull
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
        private val hyperHive: HyperHive
) : IDatabaseRepository {

    private val products: ZfmpUtz48V001 by lazy { ZfmpUtz48V001(hyperHive) } // Информация о товаре
    private val eanInfo: ZmpUtz25V001 by lazy { ZmpUtz25V001(hyperHive) } // Информация о штрих-коде
    private val dictionary: ZmpUtz17V001 by lazy { ZmpUtz17V001(hyperHive) } // Справочник с наборами данных
    private val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки
    private val units: ZmpUtz07V001 by lazy { ZmpUtz07V001(hyperHive) } // Единицы измерения
    private val printers: ZmpUtz26V001 by lazy { ZmpUtz26V001(hyperHive) } // Принтеры
    private val iconDescriptions: ZmpUtz38V001 by lazy { ZmpUtz38V001(hyperHive) } // Описание иконок
    private val taskTypes: ZmpUtz39V001 by lazy { ZmpUtz39V001(hyperHive) } // Типы заданий
    private val storages: ZmpUtz40V001 by lazy { ZmpUtz40V001(hyperHive) } // Склады
    private val allowed: ZmpUtz41V001 by lazy { ZmpUtz41V001(hyperHive) } // Разрешенные товары
    private val forbidden: ZmpUtz42V001 by lazy { ZmpUtz42V001(hyperHive) } // Запрещенные товары
    private val returnReasons: ZmpUtz44V001 by lazy { ZmpUtz44V001(hyperHive) } // Причины возврата
    private val providers: ZmpUtz09V001 by lazy { ZmpUtz09V001(hyperHive) } // Поставщики
    private val alcoCodes: ZmpUtz22V001 by lazy { ZmpUtz22V001(hyperHive) } // Алкокоды
    private val goods: ZmpUtz30V001 by lazy { ZmpUtz30V001(hyperHive) } // Товары
    private val producers: ZmpUtz43V001 by lazy { ZmpUtz43V001(hyperHive) } // Производители
    private val markGroup: ZmpUtz109V001 by lazy { ZmpUtz109V001(hyperHive) } //группы маркировки

    private var markTypeGroups: Set<MarkTypeGroup> = mutableSetOf()

    override suspend fun getGoodInfoByMaterial(material: String): GoodInfo? {
        return withContext(Dispatchers.IO) {
            products.getProductInfoByMaterial(material)?.let { goodInfo ->

                GoodInfo(
                        ean = getEanByMaterialUnits(material, goodInfo.buom.orEmpty()),
                        eans = getEanMapByMaterialUnits(material, goodInfo.buom.orEmpty()),
                        material = material,
                        name = goodInfo.name.orEmpty(),
                        kind = goodInfo.getGoodKind(),
                        section = goodInfo.abtnr.orEmpty(),
                        matrix = getMatrixType(goodInfo.matrType.orEmpty()),
                        markType = enumValueOrNull<MarkType>(goodInfo.markType.orEmpty()).orIfNull { MarkType.UNKNOWN }
                )
            }
        }
    }

    private suspend fun getEanByMaterialUnits(material: String, unitsCode: String): String {
        return withContext(Dispatchers.IO) {
            eanInfo.getEanInfoByMaterialUnits(material, unitsCode)?.toEanInfo()?.ean.orEmpty()
        }
    }

    override suspend fun getEanListByMaterialUnits(material: String, unitsCode: String): List<String> {
        return withContext(Dispatchers.IO) {
            eanInfo.getEanListByMaterialUnits(material, unitsCode)
        }
    }

    override suspend fun getEanMapByMaterialUnits(material: String, unitsCode: String): MutableMap<String, Float> {
        return withContext(Dispatchers.IO) {
            eanInfo.getEanMapByMaterialUnits(material, unitsCode)
        }
    }

    override suspend fun getEanInfo(ean: String): com.lenta.shared.requests.combined.scan_info.pojo.EanInfo? {
        return eanInfo.getEanInfo(ean)?.toEanInfo()
    }

    override suspend fun getAllowedAppVersion(): String? {
        return withContext(Dispatchers.IO) {
            settings.getAllowedBksAppVersion()
        }
    }

    override suspend fun getUnitsByCode(code: String): Uom {
        return withContext(Dispatchers.IO) {
            units.getUnitName(code)?.let { name ->
                Uom(code, name)
            } ?: Uom.ST
        }
    }

    override suspend fun getTaskTypeList(): List<TaskType> {
        return withContext(Dispatchers.IO) {
            taskTypes.getTaskTypeList()
        }
    }

    override suspend fun getTaskType(code: String): TaskType? {
        return withContext(Dispatchers.IO) {
            taskTypes.getTaskType(code)
        }
    }

    override suspend fun getStorageList(taskType: String): List<String> {
        return withContext(Dispatchers.IO) {
            storages.getStorageList(taskType)
        }
    }

    override suspend fun getReturnReason(taskType: String, reasonCode: String): ReturnReason? {
        return withContext(Dispatchers.IO) {
            returnReasons.getReturnReason(taskType, reasonCode)
        }
    }

    override suspend fun getReturnReasonList(taskType: String): List<ReturnReason> {
        return withContext(Dispatchers.IO) {
            returnReasons.getReturnReasonList(taskType)
        }
    }

    override suspend fun getTaskAttributes(taskType: String): Set<String> {
        return withContext(Dispatchers.IO) {
            allowed.getTaskAttributeList(taskType)
        }
    }

    override suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult, taskType: String): Boolean {
        return withContext(Dispatchers.IO) {
            if (goodInfo.materialInfo?.isVet.isSapTrue()) {
                return@withContext false
            }

            // Параметры товара
            val controlType = goodInfo.getControlType().code
            val goodType = goodInfo.materialInfo?.goodType
            val goodGroup = goodInfo.materialInfo?.goodGroup
            val purchaseGroup = goodInfo.materialInfo?.purchaseGroup

            // Таблицы с параметрами
            val allowedParams = allowed.getAllParams(taskType)
            val forbiddenParams = forbidden.getAllParams(taskType)

            // Поиск по таблице разрешенных параметров
            val allowedList = allowedParams
                    .filter { it.controlType == controlType }
                    .filter { it.goodType == goodType }
                    .filter { it.goodGroup == if (goodGroup.orEmpty().isNotEmpty()) goodGroup else it.goodGroup }
                    .filter { it.purchaseGroup == if (purchaseGroup.orEmpty().isNotEmpty()) purchaseGroup else it.purchaseGroup }

            if (allowedList.isNotEmpty()) {
                return@withContext true
            }

            // Поиск по таблице запрещенных параметров
            val forbiddenList = forbiddenParams
                    .filter { it.controlType == controlType }
                    .filter { it.goodType == goodType }
                    .filter { it.goodGroup == if (goodGroup.orEmpty().isNotEmpty()) goodGroup else it.goodGroup }
                    .filter { it.purchaseGroup == if (purchaseGroup.orEmpty().isNotEmpty()) purchaseGroup else it.purchaseGroup }

            if (forbiddenList.isNotEmpty()) {
                return@withContext false
            }

            true
        }
    }

    override suspend fun getProviderInfo(code: String): ProviderInfo? {
        return withContext(Dispatchers.IO) {
            providers.getProviderInfo(code)
        }
    }

    override suspend fun getAlcoCodeInfoList(alcoCode: String): List<AlcoCodeInfo> {
        return withContext(Dispatchers.IO) {
            alcoCodes.getAlcoCodeInfoList(alcoCode)
        }
    }

    override suspend fun getBasketVolume(): Double? {
        return withContext(Dispatchers.IO) {
            settings.getBKSBasketVolume()
        }
    }

    override suspend fun isMarkTypeInDatabase(markType: MarkType): Boolean {
        return withContext(Dispatchers.IO) {
            val markTypeString = markType.name
            markGroup.isMarkTypeInDatabase(markTypeString)
        }
    }

    override suspend fun getMarkTypeGroupByMarkType(markType: MarkType): MarkTypeGroup? {
        return getMarkTypeGroups().firstOrNull() {
            it.markTypes.contains(markType)
        }
    }

    override suspend fun getMarkTypeGroupByCode(markTypeGroupCode: String?): MarkTypeGroup? {
        return getMarkTypeGroups().firstOrNull() { it.code == markTypeGroupCode }
    }

    private suspend fun getMarkTypeGroups(): Set<MarkTypeGroup> {

        return withContext(Dispatchers.IO) {
            if (markTypeGroups.isEmpty()) {
                markTypeGroups = markGroup.getMarkTypeGroups()
            }
            Logg.e { markTypeGroups.toString() }
            markTypeGroups
        }

    }
}

interface IDatabaseRepository {
    suspend fun getGoodInfoByMaterial(material: String): GoodInfo?
    suspend fun getEanListByMaterialUnits(material: String, unitsCode: String): List<String>
    suspend fun getEanMapByMaterialUnits(material: String, unitsCode: String): MutableMap<String, Float>
    suspend fun getEanInfo(ean: String): com.lenta.shared.requests.combined.scan_info.pojo.EanInfo?
    suspend fun getAllowedAppVersion(): String?
    suspend fun getUnitsByCode(code: String): Uom
    suspend fun getTaskTypeList(): List<TaskType>
    suspend fun getTaskType(code: String): TaskType?
    suspend fun getStorageList(taskType: String): List<String>
    suspend fun getReturnReason(taskType: String, reasonCode: String): ReturnReason?
    suspend fun getReturnReasonList(taskType: String): List<ReturnReason>
    suspend fun getTaskAttributes(taskType: String): Set<String>
    suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult, taskType: String): Boolean
    suspend fun getProviderInfo(code: String): ProviderInfo?
    suspend fun getAlcoCodeInfoList(alcoCode: String): List<AlcoCodeInfo>
    suspend fun getBasketVolume(): Double?
    suspend fun isMarkTypeInDatabase(markType: MarkType): Boolean
    suspend fun getMarkTypeGroupByMarkType(markType: MarkType): MarkTypeGroup?
    suspend fun getMarkTypeGroupByCode(markTypeGroupCode: String?): MarkTypeGroup?
}