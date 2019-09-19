package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName

//ET_DOC_CHK - Таблица сверки документов по поставке
data class DeliveryDocumentRevise(
        val documentID: String, //ID Документа
        val documentName: String, //Название документа
        val isObligatory: Boolean,  // ??? - Общий флаг
        var isCheck: Boolean,   // ??? - Общий флаг
        val documentType: DocumentType // Тип документа
) {

    companion object {
        fun from(restData: DeliveryDocumentReviseRestData): DeliveryDocumentRevise {
            return DeliveryDocumentRevise(
                    documentID = restData.documentID,
                    documentName = restData.documentName,
                    isObligatory = restData.isObligatory.isNotEmpty(),
                    isCheck = restData.isCheck.isNotEmpty(),
                    documentType = DocumentType.from(restData.documentType)
            )
        }
    }
}

data class DeliveryDocumentReviseRestData(
        @SerializedName("DOC_ID")
        val documentID: String,
        @SerializedName("DOC_NAME")
        val documentName: String,
        @SerializedName("OBLIGATORY")
        val isObligatory: String,
        @SerializedName("FLG_CHECK")
        val isCheck: String,
        @SerializedName("DOC_TYPE")
        val documentType: String) {

    companion object {
        fun from(data: DeliveryDocumentRevise): DeliveryDocumentReviseRestData {
            return DeliveryDocumentReviseRestData(
                    documentID = data.documentID,
                    documentName = data.documentName,
                    isObligatory = if (data.isObligatory) "X" else "",
                    isCheck = if (data.isCheck) "X" else "",
                    documentType = data.documentType.documentTypeString
            )
        }
    }
}

//TODO: find and add all document types
enum class DocumentType(val documentTypeString: String) {
    None(""),
    Simple("0"),
    Invoice("1");

    companion object {
        fun from(documentTypeString: String): DocumentType {
            return when (documentTypeString) {
                "0" -> Simple
                "2" -> Invoice
                else -> None
            }
        }
    }
}