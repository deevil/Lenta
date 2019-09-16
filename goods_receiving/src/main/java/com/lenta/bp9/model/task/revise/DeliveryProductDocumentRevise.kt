package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName

//ET_DOC_MATNR_CHK - Таблица для сверки документов поставки
data class DeliveryProductDocumentRevise(
        val productNumber: String, // Номер товара
        val documentID: String, // ID Документа
        val documentName: String, // Название документа
        val isObligatory: Boolean,  // ??? - Общий флаг
        val isCheck: Boolean,   // ??? - Общий флаг
        val documentType: DocumentType, // Тип документа
        val isSet: Boolean, // УТЗ ТСД: Индикатор: Признак набора
        val initialCount: Int, // Исходное количество позиции поставки
        val measureUnits: String // Продажная ЕИ
) {

    companion object {
        fun from(restData: DeliveryProductDocumentReviseRestData): DeliveryProductDocumentRevise {
            return DeliveryProductDocumentRevise(
                    productNumber = restData.productNumber,
                    documentID = restData.documentID,
                    documentName = restData.documentName,
                    isObligatory = restData.isObligatory.isNotEmpty(),
                    isCheck = restData.isCheck.isNotEmpty(),
                    documentType = DocumentType.from(restData.documentType),
                    isSet = restData.isSet.isNotEmpty(),
                    initialCount = restData.initialCount.toInt(),
                    measureUnits = restData.measureUnits
            )
        }
    }
}

data class DeliveryProductDocumentReviseRestData(
        @SerializedName("MATNR")
        val productNumber: String,
        @SerializedName("DOC_ID")
        val documentID: String,
        @SerializedName("DOC_NAME")
        val documentName: String,
        @SerializedName("OBLIGATORY")
        val isObligatory: String,
        @SerializedName("FLG_CHECK")
        val isCheck: String,
        @SerializedName("DOC_TYPE")
        val documentType: String,
        @SerializedName("IS_SET")
        val isSet: String,
        @SerializedName("ORMNG")
        val initialCount: String,
        @SerializedName("VRKME")
        val measureUnits: String) {

    companion object {
        fun from(data: DeliveryProductDocumentRevise): DeliveryProductDocumentReviseRestData {
            return DeliveryProductDocumentReviseRestData(
                    productNumber = data.productNumber,
                    documentID = data.documentID,
                    documentName = data.documentName,
                    isObligatory = if (data.isObligatory) "X" else "",
                    isCheck = if (data.isCheck) "X" else "",
                    documentType = data.documentType.documentTypeString,
                    isSet = if (data.isSet) "X" else "",
                    initialCount = data.initialCount.toString(),
                    measureUnits = data.measureUnits
            )
        }
    }
}