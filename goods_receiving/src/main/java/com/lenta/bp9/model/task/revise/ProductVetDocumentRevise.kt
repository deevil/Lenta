package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName
import com.lenta.shared.utilities.extentions.toStringFormatted

//ET_VET_CHK - Таблица сверки ВСД по товарам
data class ProductVetDocumentRevise(
        val productNumber: String, // Номер товара
        val vetDocumentID: String, // ID ветеринарно- сопроводительного документа
        val attachType: String, // Тип сопоставления ВСД (Меркурий)
        val isAttached: Boolean, // ??? - Общий флаг
        val productName: String, // Название продукции
        val volume: Double, // Объём груза
        val measureUnits: String, // Продажная ЕИ
        val originProductName: String, // Наименование продукта
        val isCheck: Boolean, // ??? - Общий флаг
        val attachText: String // ??? - Текст 30 знаков
) {

    companion object {
        fun from(restData: ProductVetDocumentReviseRestData): ProductVetDocumentRevise {
            return ProductVetDocumentRevise(
                    productNumber = restData.productNumber,
                    vetDocumentID = restData.vetDocumentID,
                    attachType = restData.attachType,
                    isAttached = restData.isAttached.isNotEmpty(),
                    productName = restData.productName,
                    volume = restData.volume.toDouble(),
                    measureUnits = restData.measureUnits,
                    originProductName = restData.originProductName,
                    isCheck = restData.isCheck.isNotEmpty(),
                    attachText = restData.attachText
            )
        }
    }
}

data class ProductVetDocumentReviseRestData(
        @SerializedName("MATNR")
        val productNumber: String,
        @SerializedName("VSDID")
        val vetDocumentID: String,
        @SerializedName("ATTACH_TYPE")
        val attachType: String,
        @SerializedName("IS_ATTACHED")
        val isAttached: String,
        @SerializedName("PRODUCTITEMNAME")
        val productName: String,
        @SerializedName("VSDVOLUME")
        val volume: String,
        @SerializedName("VRKME")
        val measureUnits: String,
        @SerializedName("ORIGINPRODITEMNAME")
        val originProductName: String,
        @SerializedName("FLG_CHECK")
        val isCheck: String,
        @SerializedName("ATTACH_TXT")
        val attachText: String
) {

    companion object {
        fun from(data: ProductVetDocumentRevise): ProductVetDocumentReviseRestData {
            return ProductVetDocumentReviseRestData(
                    productNumber = data.productNumber,
                    vetDocumentID = data.vetDocumentID,
                    attachType = data.attachType,
                    isAttached = if (data.isAttached) "X" else "",
                    productName = data.productName,
                    volume = data.volume.toStringFormatted(),
                    measureUnits = data.measureUnits,
                    originProductName = data.originProductName,
                    isCheck = if (data.isCheck) "X" else "",
                    attachText = data.attachText
            )
        }
    }
}