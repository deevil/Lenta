package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TaskTransportMarriageInfo(
        val cargoUnitNumber: String,
        val processingUnitNumber: String,
        val materialNumber: String,
        val batchNumber: String,
        var quantity: Double,
        var uom: Uom )
{

    companion object {
        suspend fun from(hyperHive: HyperHive, restData: TaskTransportMarriageInfoRestData): TaskTransportMarriageInfo {
            return withContext(Dispatchers.IO) {
                val zmpUtz07V001: ZmpUtz07V001 by lazy {
                    ZmpUtz07V001(hyperHive)
                }
                val uomInfo = zmpUtz07V001.getUomInfo(restData.uom)
                return@withContext TaskTransportMarriageInfo(
                        cargoUnitNumber = restData.cargoUnitNumber,
                        processingUnitNumber = restData.processingUnitNumber,
                        materialNumber = restData.materialNumber,
                        batchNumber = restData.batchNumber,
                        quantity = restData.quantity.toDouble() ?: 0.0,
                        uom = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name ?: "")
                )
            }

        }
    }
}

data class TaskTransportMarriageInfoRestData(
        @SerializedName("EXIDV_TOP") //Номер ГЕ
        val cargoUnitNumber: String,
        @SerializedName("EXIDV") //Номер ЕО
        val processingUnitNumber: String,
        @SerializedName("MATNR") //Номер товара
        val materialNumber: String,
        @SerializedName("ZCHARG") //Номер партии
        val batchNumber: String,
        @SerializedName("MENGE") //Количество
        var quantity: String,
        @SerializedName("MEINS")
        var uom: String //Базисная единица измерения
) {

    companion object {
        fun from(data: TaskTransportMarriageInfo): TaskTransportMarriageInfoRestData {
            return TaskTransportMarriageInfoRestData(
                    cargoUnitNumber = data.cargoUnitNumber,
                    processingUnitNumber = data.processingUnitNumber,
                    materialNumber = data.materialNumber,
                    batchNumber = data.batchNumber,
                    quantity = data.quantity.toString(),
                    uom = data.uom.code
            )
        }
    }
}