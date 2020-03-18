package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//ET_TASK_DIFF Таблица расхождений по товару
data class TaskProductDiscrepancies(
        val materialNumber: String,
        val exidv: String, //Номер ЕО
        val numberDiscrepancies: String, //Количество расхождения в ПЕИ
        val uom: Uom,
        val typeDiscrepancies: String, //Тип расхождения
        val isNotEdit: Boolean,
        val isNew: Boolean,
        val notEditNumberDiscrepancies: String ////Количество нередактируемого расхождения, заполняется из numberDiscrepancies при получении таблицы ET_TASK_DIFF в рестах
) {

    companion object {
        suspend fun from(hyperHive: HyperHive, restData: TaskProductDiscrepanciesRestData): TaskProductDiscrepancies {
            return withContext(Dispatchers.IO) {
                val zmpUtz07V001: ZmpUtz07V001 by lazy {
                    ZmpUtz07V001(hyperHive)
                }
                val uomInfo = zmpUtz07V001.getUomInfo(restData.unit)
                return@withContext TaskProductDiscrepancies(
                        materialNumber = restData.materialNumber,
                        exidv = restData.exidv,
                        numberDiscrepancies = restData.numberDiscrepancies,
                        uom = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name ?: ""),
                        typeDiscrepancies = restData.typeDiscrepancies,
                        isNotEdit = restData.isNotEdit.isNotEmpty(),
                        isNew = restData.isNew.isNotEmpty(),
                        notEditNumberDiscrepancies = if (restData.isNotEdit.isNotEmpty()) restData.numberDiscrepancies else ""
                )
            }

        }
    }
}

data class TaskProductDiscrepanciesRestData(
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
        fun from(data: TaskProductDiscrepancies): TaskProductDiscrepanciesRestData {
            return TaskProductDiscrepanciesRestData(
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