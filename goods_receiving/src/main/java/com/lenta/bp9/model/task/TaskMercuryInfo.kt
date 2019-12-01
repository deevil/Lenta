package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.models.core.Uom

//ET_VET_DIFF Таблица расхождений по вет.товарам
data class TaskMercuryInfo(
        val materialNumber: String,
        val vetDocumentID: String, //ID ветеринарно-сопроводительного документа
        val volume: Double, //Объём груза
        val uom: Uom, //Продажная ЕИ
        val typeDiscrepancies: String, //Тип расхождения (Причина отклонения при ППП)
        val numberDiscrepancies: Double, //Количество расхождения в ПЕИ (Отклонение от фактически поставленного количества в ПЕИ)
        val productionDates: List<String>,	//даты производства
        val manufacturers: List<String>,	//производители
        val productionDateTo: String	//Поле типа DATS

)
data class TaskMercuryInfoRestData(
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
        val productionDates: List<String>,
        @SerializedName("PROD_NAME")
        val manufacturers: List<String>,
        @SerializedName("PRODDATE_TO")
        val productionDateTo: String
) {

    companion object {
        fun from(data: TaskMercuryInfo?): TaskMercuryInfoRestData? {
            return if (data == null) null else {
                TaskMercuryInfoRestData(
                        materialNumber= data.materialNumber,
                        vetDocumentID = data.vetDocumentID,
                        volume = data.volume.toString(),
                        unit = data.uom.code,
                        typeDiscrepancies = data.typeDiscrepancies,
                        numberDiscrepancies = data.numberDiscrepancies.toString(),
                        productionDates = data.productionDates,
                        manufacturers = data.manufacturers,
                        productionDateTo = data.productionDateTo
                )
            }
        }
    }
}