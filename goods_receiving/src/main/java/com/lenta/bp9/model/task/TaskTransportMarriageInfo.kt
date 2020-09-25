package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TaskTransportMarriageInfo(
        val cargoUnitNumber: String,
        val processingUnitNumber: String,
        val materialNumber: String,
        val materialName: String,
        val batchNumber: String,
        val quantity: Double,
        val quantityInvestments: Double,
        val uom: Uom,
        val sectionId: String)
{

    companion object {
        suspend fun from(hyperHive: HyperHive, restData: TaskProcessingUnitInfoRestData, cargoUnitNumber: String, batchNumber: String): TaskTransportMarriageInfo {
            return withContext(Dispatchers.IO) {
                val zfmpUtz48V001: ZfmpUtz48V001 = ZfmpUtz48V001(hyperHive)
                val zmpUtz07V001: ZmpUtz07V001 = ZmpUtz07V001(hyperHive)

                val materialInfo = zfmpUtz48V001.getProductInfoByMaterial(restData.materialNumber)
                val uomInfo = zmpUtz07V001.getUomInfo(restData.uom)

                TaskTransportMarriageInfo(
                        cargoUnitNumber = cargoUnitNumber,
                        processingUnitNumber = restData.processingUnitNumber.orEmpty(),
                        materialNumber = materialInfo?.material.orEmpty(),
                        materialName = materialInfo?.name.orEmpty(),
                        batchNumber = batchNumber,
                        quantity = 0.0,
                        quantityInvestments = restData.menge?.toDouble() ?: 0.0,
                        uom = Uom(code = uomInfo?.uom.orEmpty(), name = uomInfo?.name.orEmpty()),
                        sectionId = materialInfo?.abtnr.orEmpty()
                )
            }

        }
    }

    fun getMaterialLastSix(): String {
        return if (materialNumber.length > 6)
            materialNumber.substring(materialNumber.length - 6)
        else
            materialNumber
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