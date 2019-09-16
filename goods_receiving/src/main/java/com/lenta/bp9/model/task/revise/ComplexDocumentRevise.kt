package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName

//ET_CMPLX_DOC - Позиции сверки составного документа
data class ComplexDocumentRevise(
        val documentID: String, // ID Документа
        val conditionID: String, // ID Условия составного документа
        val conditionName: String, // Название условия составного документа
        val isCheck: Boolean // ??? - Общий флаг
) {

    companion object {
        fun from(restData: ComplexDocumentReviseRestData): ComplexDocumentRevise {
            return ComplexDocumentRevise(
                    documentID = restData.documentID,
                    conditionID = restData.conditionID,
                    conditionName = restData.conditionName,
                    isCheck = restData.isCheck.isNotEmpty()
            )
        }
    }
}

data class ComplexDocumentReviseRestData(
        @SerializedName("DOC_ID")
        val documentID: String,
        @SerializedName("DOC_COND_ID")
        val conditionID: String,
        @SerializedName("DOC_COND_NAME")
        val conditionName: String,
        @SerializedName("FLG_CHECK")
        val isCheck: String) {

    companion object {
        fun from(data: ComplexDocumentRevise): ComplexDocumentReviseRestData {
            return ComplexDocumentReviseRestData(
                    documentID = data.documentID,
                    conditionID = data.conditionID,
                    conditionName = data.conditionName,
                    isCheck = if (data.isCheck) "X" else ""
            )
        }
    }
}