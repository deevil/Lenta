package com.lenta.bp16.repository

import com.lenta.bp16.request.pojo.WarehouseInfo
import com.lenta.bp16.model.pojo.Good
import com.lenta.bp16.model.pojo.GoodInfo
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz106V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz17V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.pojo.EanInfo
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
    private val dictionary: ZmpUtz17V001 by lazy { ZmpUtz17V001(hyperHive) } // Справочник с наборами данных
    private val warehouses: ZmpUtz106V001 by lazy { ZmpUtz106V001(hyperHive) } // Справочник складов
    private val barcodeInfo: ZmpUtz25V001 by lazy { ZmpUtz25V001(hyperHive) } // Информация о  штрихкоде
    private val productInfo: ZfmpUtz48V001 by lazy { ZfmpUtz48V001(hyperHive) } // Информация о товаре

    override suspend fun getAllowedAppVersion(): String? {
        return withContext(Dispatchers.IO) {
            settings.getAllowedProAppVersion()
        }
    }

    override suspend fun getUnitsByCode(code: String): Uom {
        return withContext(Dispatchers.IO) {
            units.getUnitName(code)?.toLowerCase(Locale.getDefault())?.let { name ->
                Uom(code, name)
            } ?: Uom.KG
        }
    }

    override suspend fun getServerAddress(): String? {
        return withContext(Dispatchers.IO) {
            settings.getServerAddress()
        }
    }

    override suspend fun getPcpContTimeMm(): Int {
        return withContext(Dispatchers.IO) {
            settings.getPcpContTimeMm()?.toIntOrNull() ?: 0
        }
    }

    override suspend fun getPcpExpirTimeMm(): Int {
        return withContext(Dispatchers.IO) {
            settings.getPcpExpirTimeMm()?.toIntOrNull() ?: 0
        }
    }

    override suspend fun getLabelLimit(): Int {
        return withContext(Dispatchers.IO) {
            settings.getLabelLimit()?.toIntOrNull() ?: 0
        }
    }

    override suspend fun getCategoryList(): List<DictElement> {
        return withContext(Dispatchers.IO) {
            val list = dictionary.getItemsByTidSorted("025")?.toElementList()?.toMutableList()
                    ?: mutableListOf()
            if (list.size > 1) {
                list.add(0, DictElement(
                        code = "0",
                        order = "0",
                        description = ""
                ))
            }

            list
        }
    }

    override suspend fun getDefectList(): List<DictElement> {
        return withContext(Dispatchers.IO) {
            val list = dictionary.getItemsByTidSorted("024")?.toElementList()?.toMutableList()
                    ?: mutableListOf()
            if (list.size > 1) {
                list.add(0, DictElement(
                        code = "0",
                        order = "0",
                        description = ""
                ))
            }

            list
        }
    }

    override suspend fun getCategory(categoryCode: String): DictElement? {
        return withContext(Dispatchers.IO) {
            getCategoryList().find { it.code == categoryCode }
        }
    }

    override suspend fun getDefect(defectCode: String): DictElement? {
        return withContext(Dispatchers.IO) {
            var formattedCode = defectCode
            while (formattedCode.startsWith("0")) {
                formattedCode = formattedCode.substring(1)
            }
            getDefectList().find { it.code == formattedCode }
        }
    }

    override suspend fun getGoodByEan(ean: String): GoodInfo? {
        return withContext(Dispatchers.IO) {
            getEanInfoByEan(ean)?.run {
                val product = productInfo.getProductInfoByMaterial(this.materialNumber)
                val unitName = units.getUnitName(product?.buom)
                GoodInfo(
                        ean = ean,
                        material = this.materialNumber,
                        matcode = product?.matcode.orEmpty(),
                        name = product?.name.orEmpty(),
                        uom = Uom(
                                code = product?.buom.orEmpty(),
                                name = unitName.orEmpty())
                )
            }
        }
    }

    override suspend fun getEanInfoByEan(ean: String): EanInfo? {
        return withContext(Dispatchers.IO) {
            barcodeInfo.getEanInfo(ean)?.toEanInfo()
        }
    }

    override suspend fun getProFillCondition(): String?{
        return withContext(Dispatchers.IO){
            settings.getProFillCondition()
        }
    }

    override suspend fun getIncludeCondition(): String? {
        return withContext(Dispatchers.IO){
            settings.getIncludeCondition()
        }
    }

    override suspend fun getWarehouses(tkNumber: String): List<WarehouseInfo> {
        return withContext(Dispatchers.IO) {
            val warehousesList = warehouses.getWarehouseNumbers(tkNumber)
            warehousesList.mapNotNull { warehouseModel ->
                warehouseModel.name?.run {
                    WarehouseInfo(
                            name = warehouseModel.name.orEmpty(),
                            werks = tkNumber,
                            lgort = warehouseModel.lgort.orEmpty()
                    )
                }
            }
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
    suspend fun getGoodByEan(ean: String): GoodInfo?
    suspend fun getEanInfoByEan(ean: String): EanInfo?
    suspend fun getProFillCondition(): String?
    suspend fun getIncludeCondition(): String?

    suspend fun getWarehouses(tkNumber: String): List<WarehouseInfo>
}