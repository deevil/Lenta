package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName

//ET_PARTS_CHK - Таблица сверки партий по товарам
data class ProductBatchRevise(
        val productNumber: String, // Номер товара
        val batchNumber: String, // Номер партии
        val text1: String, // ??? - Текст длиной 20 знаков
        val text2: String, // ??? - Текст длиной 20 знаков
        val quantity: Int, // ??? - Натуральное число
        var isCheck: Boolean,   // ??? - Общий флаг
        val matnrOSN: String // ??? - Номер товара
) {

    companion object {
        fun from(restData: ProductBatchReviseRestData): ProductBatchRevise {
            return ProductBatchRevise(
                    productNumber = restData.productNumber,
                    batchNumber = restData.batchNumber,
                    text1 = restData.text1,
                    text2 = restData.text2,
                    quantity = restData.quantity.toInt(),
                    isCheck = restData.isCheck.isNotEmpty(),
                    matnrOSN = restData.matnrOSN
            )
        }
    }
}

data class ProductBatchReviseRestData(
        @SerializedName("MATNR")
        val productNumber: String,
        @SerializedName("ZCHARG")
        val batchNumber: String,
        @SerializedName("TEXT1")
        val text1: String,
        @SerializedName("TEXT2")
        val text2: String,
        @SerializedName("QNT_POS")
        val quantity: String,
        @SerializedName("FLG_CHECK")
        val isCheck: String,
        @SerializedName("MATNR_OSN")
        val matnrOSN: String) {

    companion object {
        fun from(data: ProductBatchRevise): ProductBatchReviseRestData {
            return ProductBatchReviseRestData(
                    productNumber = data.productNumber,
                    batchNumber = data.batchNumber,
                    text1 = data.text1,
                    text2 = data.text2,
                    quantity = data.quantity.toString(),
                    isCheck = if (data.isCheck) "X" else "",
                    matnrOSN = data.matnrOSN
            )
        }
    }
}