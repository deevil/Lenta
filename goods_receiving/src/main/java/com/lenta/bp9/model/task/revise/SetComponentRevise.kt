package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName

//ET_COMP_CHK - Таблица результатов сверки документов для компонентов набора
data class SetComponentRevise(
        val productNumber: String, // Номер товара
        val isCheck: Boolean,   // ??? - Общий флаг
        val matnrOSN: String // ??? - Номер товара
) {

    companion object {
        fun from(restData: SetComponentReviseRestData): SetComponentRevise {
            return SetComponentRevise(
                    productNumber = restData.productNumber,
                    isCheck = restData.isCheck.isNotEmpty(),
                    matnrOSN = restData.matnrOSN
            )
        }
    }
}

data class SetComponentReviseRestData(
        @SerializedName("MATNR")
        val productNumber: String,
        @SerializedName("FLG_CHECK")
        val isCheck: String,
        @SerializedName("MATNR_OSN")
        val matnrOSN: String) {

    companion object {
        fun from(data: SetComponentRevise): SetComponentReviseRestData {
            return SetComponentReviseRestData(
                    productNumber = data.productNumber,
                    isCheck = if (data.isCheck) "X" else "",
                    matnrOSN = data.matnrOSN
            )
        }
    }
}