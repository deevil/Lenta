package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//ET_PARTS_DIFF Таблица расхождений по партиям
data class TaskBatchesDiscrepancies(
        val materialNumber: String,
        val exidv: String, //Номер ЕО
        val numberDiscrepancies: String, //Количество расхождения в ПЕИ
        val uom: Uom,
        val typeDiscrepancies: String, //Тип расхождения
        val isNotEdit: Boolean,
        val isNew: Boolean
) {

    companion object {
        suspend fun from(hyperHive: HyperHive, restData: TaskBatchesDiscrepanciesRestData): TaskBatchesDiscrepancies {
            return withContext(Dispatchers.IO) {
                val zmpUtz07V001: ZmpUtz07V001 by lazy {
                    ZmpUtz07V001(hyperHive)
                }
                val uomInfo = zmpUtz07V001.getUomInfo(restData.unit)
                return@withContext TaskBatchesDiscrepancies(
                        materialNumber = restData.materialNumber,
                        exidv = restData.exidv,
                        numberDiscrepancies = restData.numberDiscrepancies,
                        uom = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name ?: ""),
                        typeDiscrepancies = restData.typeDiscrepancies,
                        isNotEdit = restData.isNotEdit.isNotEmpty(),
                        isNew = restData.isNew.isNotEmpty()
                )
            }

        }
    }
}

data class TaskBatchesDiscrepanciesRestData(
        @SerializedName("MATNR")
        val materialNumber: String,
        @SerializedName("EXIDV")
        val exidv: String,
        @SerializedName("LFIMG_DIFF")
        val numberDiscrepancies: String,
        @SerializedName("VRKME")
        var unit: String,
        @SerializedName("GRUND")
        val typeDiscrepancies: String,
        @SerializedName("NOT_EDIT")
        val isNotEdit: String,
        @SerializedName("IS_NEW")
        val isNew: String
) {

    companion object {
        fun from(data: TaskBatchesDiscrepancies): TaskBatchesDiscrepanciesRestData {
            return TaskBatchesDiscrepanciesRestData(
                    materialNumber = data.materialNumber,
                    exidv = data.exidv,
                    numberDiscrepancies = data.numberDiscrepancies,
                    unit = data.uom.code,
                    typeDiscrepancies = data.typeDiscrepancies,
                    isNotEdit = if (data.isNotEdit) "X" else "",
                    isNew = if (data.isNew) "X" else ""
            )
        }
    }
}