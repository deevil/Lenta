package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//ET_EXIDV Таблица ЕО (Единица обработки)
data class TaskProcessingUnitInfo(
        val materialNumber: String,
        val processingUnitNumber: String,
        val menge: Double,
        val uom: Uom,
        val isAlco: Boolean,
        val isExc: Boolean,
        val purchaseOrderUnits: Uom,
        val quantityInvestments: Double,
        val isBoxFl: Boolean,
        val isStampFl: Boolean
) {
    companion object {
        suspend fun from(hyperHive: HyperHive, restData: TaskProcessingUnitInfoRestData): TaskProcessingUnitInfo {
            return withContext(Dispatchers.IO) {
                val zmpUtz07V001: ZmpUtz07V001 by lazy {
                    ZmpUtz07V001(hyperHive)
                }
                val uomInfo = zmpUtz07V001.getUomInfo(restData.uom)
                val purchaseOrderUnitUomInfo = zmpUtz07V001.getUomInfo(restData.purchaseOrderUnits)
                return@withContext TaskProcessingUnitInfo(
                        materialNumber = restData.materialNumber.orEmpty(),
                        processingUnitNumber = restData.processingUnitNumber.orEmpty(),
                        menge = restData.menge?.toDouble() ?: 0.0,
                        uom = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name.orEmpty()),
                        isAlco = restData.isAlco?.isNotEmpty() == true,
                        isExc = restData.isExc?.isNotEmpty() == true,
                        purchaseOrderUnits = Uom(code = purchaseOrderUnitUomInfo?.uom.orEmpty(), name = purchaseOrderUnitUomInfo?.name.orEmpty()),
                        quantityInvestments = restData.quantityInvestments?.toDouble() ?: 0.0,
                        isBoxFl = restData.isBoxFl?.isNotEmpty() == true,
                        isStampFl = restData.isStampFl?.isNotEmpty() == true
                )
            }
        }
    }
}

data class TaskProcessingUnitInfoRestData(
        //Номер товара
        @SerializedName("MATNR")
        val materialNumber: String?,
        //Номер ЕО
        @SerializedName("EXIDV")
        val processingUnitNumber: String?,
        //Кол-во в заказе
        @SerializedName("MENGE")
        val menge: String?,
        //базисная единица измерения
        @SerializedName("MEINS")
        val uom: String?,
        //Индикатор: Алкоголь
        @SerializedName("IS_ALCO")
        val isAlco: String?,
        //Признак – товар акцизный
        @SerializedName("IS_EXC")
        val isExc: String?,
        //ЕИ заказа на поставку
        @SerializedName("BSTME")
        val purchaseOrderUnits: String?,
        //кол-во вложения
        @SerializedName("QNTINCL")
        val quantityInvestments: String?,
        //Общий флаг
        @SerializedName("IS_BOX_FL")
        val isBoxFl: String?,
        //Общий флаг
        @SerializedName("IS_MARK_FL")
        val isStampFl: String?
) {

    companion object {
        private const val DEFAULT_MARKER = "X"

        fun from(data: TaskProcessingUnitInfo): TaskProcessingUnitInfoRestData {
            return TaskProcessingUnitInfoRestData(
                    materialNumber = data.materialNumber,
                    processingUnitNumber = data.processingUnitNumber,
                    menge = data.menge.toString(),
                    uom = data.uom.code,
                    isAlco = if (data.isAlco) DEFAULT_MARKER else "",
                    isExc = if (data.isExc) DEFAULT_MARKER else "",
                    purchaseOrderUnits = data.purchaseOrderUnits.code,
                    quantityInvestments = data.quantityInvestments.toString(),
                    isBoxFl = if (data.isBoxFl) DEFAULT_MARKER else "",
                    isStampFl = if (data.isStampFl) DEFAULT_MARKER else ""
            )
        }
    }
}