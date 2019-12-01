package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName
import com.lenta.shared.models.core.Uom

//ET_VET_NOT_ACTUAL Таблица неактуальных ВСД
data class TaskMercuryNotActual(
        val materialNumber: String,
        val vetDocumentID: String, //ID ветеринарно-сопроводительного документа
        val volume: Double, //Объём груза
        val productName: String, //Наименование продукта
        val uom: Uom, //Универсальная ЕИ
        val productionDate: String,	//дата производства
        val manufacturer: String,	//производитель
        val productionDateTo: String	//Поле типа DATS

)

data class TaskMercuryNotActualRestData(
        @SerializedName("MATNR")
        val materialNumber: String,
        @SerializedName("VSDID")
        val vetDocumentID: String,
        @SerializedName("VSDVOLUME")
        val volume: String,
        @SerializedName("ORIGINPRODITEMNAME")
        val productName: String,
        @SerializedName("VRKME")
        val unit: String,
        @SerializedName("PRODDATE")
        val productionDate: String,
        @SerializedName("PROD_NAME")
        val manufacturer: String,
        @SerializedName("PRODDATE_TO")
        val productionDateTo: String
) {

    companion object {
        fun from(data: TaskMercuryNotActual?): TaskMercuryNotActualRestData? {
            return if (data == null) null else {
                TaskMercuryNotActualRestData(
                        materialNumber= data.materialNumber,
                        vetDocumentID = data.vetDocumentID,
                        volume = data.volume.toString(),
                        productName = data.productName,
                        unit = data.uom.code,
                        productionDate = data.productionDate,
                        manufacturer = data.manufacturer,
                        productionDateTo = data.productionDateTo
                )
            }
        }
    }
}