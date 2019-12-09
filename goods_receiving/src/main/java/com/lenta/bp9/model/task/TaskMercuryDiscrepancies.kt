package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.models.core.Uom

//IT_VET_DIFF Таблица расхождений по вет.товарам
data class TaskMercuryDiscrepancies(
        val materialNumber: String,
        val vetDocumentID: String, //ID ветеринарно-сопроводительного документа
        val volume: Double, //Объём груза
        val uom: Uom, //Продажная ЕИ
        val typeDiscrepancies: String, //Тип расхождения (Причина отклонения при ППП)
        val numberDiscrepancies: Double, //Количество расхождения в ПЕИ (Отклонение от фактически поставленного количества в ПЕИ)
        val productionDate: String,	//дата производства
        val manufacturer: String,	//производитель
        val productionDateTo: String	//Поле типа DATS
)

data class TaskMercuryDiscrepanciesRestData(
        @SerializedName("MATNR")
        val materialNumber: String,
        @SerializedName("VSDID")
        val vetDocumentID: String,
        @SerializedName("VSDVOLUME")
        val volume: String,
        @SerializedName("VRKME")
        val unit: String,
        @SerializedName("GRUND")
        val typeDiscrepancies: String,
        @SerializedName("LFIMG_DIFF")
        val numberDiscrepancies: String,
        @SerializedName("PRODDATE")
        val productionDate: String,
        @SerializedName("PROD_NAME")
        val manufacturer: String,
        @SerializedName("PRODDATE_TO")
        val productionDateTo: String
) {

    companion object {
        fun from(data: TaskMercuryDiscrepancies?): TaskMercuryDiscrepanciesRestData? {
            return if (data == null) null else {
                TaskMercuryDiscrepanciesRestData(
                        materialNumber= data.materialNumber,
                        vetDocumentID = data.vetDocumentID,
                        volume = data.volume.toString(),
                        unit = data.uom.code,
                        typeDiscrepancies = data.typeDiscrepancies,
                        numberDiscrepancies = data.numberDiscrepancies.toString(),
                        productionDate = data.productionDate,
                        manufacturer = data.manufacturer,
                        productionDateTo = data.productionDateTo
                )
            }
        }
    }
}