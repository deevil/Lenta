package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//ET_ZPARTS_DIFF Таблица расхождений по Z-партиям
data class TaskZBatchesDiscrepancies(
        val processingUnit: String, //Номер ЕО
        val materialNumber: String,
        val batchNumber: String, //Номер партии
        val numberDiscrepancies: String, //Количество расхождения в ПЕИ
        val uom: Uom, //Продажная ЕИ
        val typeDiscrepancies: String, //Тип расхождения
        val isNew: Boolean, //для ПГЕ данное поле заполняется для товаров, которые не числятся в ГЕ https://trello.com/c/Mo9AqreT
        val manufactureCode: String, //ЕГАИС Код организации/производителя
        val shelfLifeDate: String, //Срок годности до (Дата)
        val shelfLifeTime: String //Срок годности до (время)
) {

    companion object {
        suspend fun from(hyperHive: HyperHive, restData: TaskZBatchesDiscrepanciesRestData): TaskZBatchesDiscrepancies {
            return withContext(Dispatchers.IO) {
                val uomInfo = ZmpUtz07V001(hyperHive).getUomInfo(restData.uomCode)

                return@withContext TaskZBatchesDiscrepancies(
                        processingUnit = restData.processingUnit.orEmpty(),
                        materialNumber = restData.materialNumber.orEmpty(),
                        batchNumber = restData.batchNumber.orEmpty(),
                        numberDiscrepancies = restData.numberDiscrepancies.orEmpty(),
                        uom = Uom(code = uomInfo?.uom.orEmpty(), name = uomInfo?.name.orEmpty()),
                        typeDiscrepancies = restData.typeDiscrepancies.orEmpty(),
                        isNew = restData.isNew?.isNotEmpty() ?: false,
                        manufactureCode = restData.manufactureCode.orEmpty(),
                        shelfLifeDate = restData.shelfLifeDate.orEmpty(),
                        shelfLifeTime = restData.shelfLifeTime.orEmpty()
                )
            }
        }
    }
}

data class TaskZBatchesDiscrepanciesRestData(
        @SerializedName("EXIDV")
        val processingUnit: String?,
        @SerializedName("MATNR")
        val materialNumber: String?,
        @SerializedName("BATCH")
        val batchNumber: String?,
        @SerializedName("LFIMG_DIFF")
        val numberDiscrepancies: String?,
        @SerializedName("VRKME")
        var uomCode: String?,
        @SerializedName("GRUND")
        val typeDiscrepancies: String?,
        @SerializedName("IS_NEW")
        val isNew: String?,
        @SerializedName("PRODUCER")
        var manufactureCode: String?,
        @SerializedName("SHELF_LIFE_D")
        val shelfLifeDate: String?,
        @SerializedName("SHELF_LIFE_T")
        val shelfLifeTime: String?
) {
    companion object {
        fun from(data: TaskZBatchesDiscrepancies): TaskZBatchesDiscrepanciesRestData {
            return TaskZBatchesDiscrepanciesRestData(
                    processingUnit = data.processingUnit,
                    materialNumber = data.materialNumber,
                    batchNumber = data.batchNumber,
                    numberDiscrepancies = data.numberDiscrepancies,
                    uomCode = data.uom.code,
                    typeDiscrepancies = data.typeDiscrepancies,
                    isNew = if (data.isNew) "X" else "",
                    manufactureCode = data.manufactureCode,
                    shelfLifeDate = data.shelfLifeDate,
                    shelfLifeTime = data.shelfLifeTime
            )
        }
    }
}