package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.models.core.Uom

//IT_VET_DIFF Таблица расхождений по вет.товарам
data class TaskMercuryDiscrepancies(
        /**номер продукта (ET_VET_DIFF -> MATNR)*/
        val materialNumber: String,
        /**ID ветеринарно-сопроводительного документа (ET_VET_DIFF -> VSDID)*/
        val vetDocumentID: String,
        /**Объём груза (ET_VET_DIFF -> VSDVOLUME)*/
        val volume: Double,
        /**Продажная ЕИ (ET_VET_DIFF -> VRKME)*/
        val uom: Uom,
        /**Тип расхождения (Причина отклонения при ППП) (ET_VET_DIFF -> GRUND)*/
        val typeDiscrepancies: String,
        /**Количество расхождения в ПЕИ (Отклонение от фактически поставленного количества в ПЕИ) (ET_VET_DIFF -> LFIMG_DIFF)*/
        val numberDiscrepancies: Double,
        /**дата производства (ET_VET_DIFF -> PRODDATE)*/
        val productionDate: String,
        /**производитель (ET_VET_DIFF -> PROD_NAME)*/
        val manufacturer: String,
        /**Поле типа DATS (ET_VET_DIFF -> PRODDATE_TO)*/
        val productionDateTo: String
) {

    companion object {
        fun from(data: TaskMercuryInfo): TaskMercuryDiscrepancies {
            return TaskMercuryDiscrepancies(
                    materialNumber = data.materialNumber,
                    vetDocumentID = data.vetDocumentID,
                    volume = data.volume,
                    uom = data.uom,
                    typeDiscrepancies = data.typeDiscrepancies,
                    numberDiscrepancies = data.numberDiscrepancies,
                    productionDate = data.productionDate,
                    manufacturer = data.manufacturer,
                    productionDateTo = data.productionDateTo
            )
        }
    }
}

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