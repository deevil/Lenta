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
        val processingUnitNumber: String, //Номер ЕО
        val numberDiscrepancies: String, //Количество расхождения в ПЕИ
        val uom: Uom,
        val typeDiscrepancies: String, //Тип расхождения
        val isNotEdit: Boolean, //не редактируемое расхождение
        val isNew: Boolean, //для ПГЕ данное поле заполняется для товаров, которые не числятся в ГЕ https://trello.com/c/Mo9AqreT
        val notEditNumberDiscrepancies: String //Количество не редактируемого расхождения, заполняется из numberDiscrepancies при получении таблицы ET_TASK_DIFF в рестах
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
                        processingUnitNumber = restData.processingUnitNumber,
                        numberDiscrepancies = restData.numberDiscrepancies,
                        uom = Uom(code = uomInfo?.uom ?: "", name = uomInfo?.name ?: ""),
                        typeDiscrepancies = restData.typeDiscrepancies,
                        isNotEdit = restData.isNotEdit.isNotEmpty(),
                        isNew = restData.isNew.isNotEmpty(),
                        notEditNumberDiscrepancies = if (restData.isNotEdit.isNotEmpty()) restData.numberDiscrepancies else ""
                )
            }
        }

        suspend fun fromMercury(hyperHive: HyperHive, restData: TaskMercuryDiscrepanciesRestData): TaskProductDiscrepancies {
            return withContext(Dispatchers.IO) {
                val zmpUtz07V001: ZmpUtz07V001 by lazy {
                    ZmpUtz07V001(hyperHive)
                }
                val uomInfo = zmpUtz07V001.getUomInfo(restData.unit)
                return@withContext TaskProductDiscrepancies(
                        materialNumber = restData.materialNumber,
                        processingUnitNumber = "",
                        numberDiscrepancies = restData.numberDiscrepancies,
                        uom = Uom(code = uomInfo?.uom.orEmpty(), name = uomInfo?.name.orEmpty()),
                        typeDiscrepancies = restData.typeDiscrepancies,
                        isNotEdit = false,
                        isNew = false,
                        notEditNumberDiscrepancies = ""
                )
            }

        }
    }
}

data class TaskProductDiscrepanciesRestData(
        @SerializedName("MATNR")
        val materialNumber: String,
        @SerializedName("EXIDV")
        val processingUnitNumber: String,
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
                    processingUnitNumber = data.processingUnitNumber,
                    numberDiscrepancies = data.numberDiscrepancies,
                    unit = data.uom.code,
                    typeDiscrepancies = data.typeDiscrepancies,
                    isNotEdit = if (data.isNotEdit) "X" else "",
                    isNew = if (data.isNew) "X" else ""
            )
        }
    }
}