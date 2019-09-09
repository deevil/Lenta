package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName

//ET_FORMA_RUS - Таблица проверки справок А и Б (Отечественный товар)
data class FormABRussianRevise(
        val productNumber: String, // Номер товара
        val batchNumber: String, // Номер партии
        val alcoCode: String, // Номер товара в ЕГАИС (АлкоКод)
        val alcoCodeName: String, // Длинное наименование
        val dateOfPour: String, // УТЗ ТСД: Дата розлива
        val EGAISName: String, // Наименование ( ЕГАИС )
        val EGAISAddress: String, // Адрес ( ЕГАИС )
        val longName: String, // Длинное наименование
        val quantityByTTN: Int, // Количество по ТТН/ГТД
        val numberTTN: String, // Номер ТТН/Номер ГТД
        val shipDate: String, // ??? - Дата
        val isCheck: Boolean, // ??? - Общий флаг
        val matnrOSN: String // ??? - Номер товара
) {

    companion object {
        fun from(restData: FormABRussianReviseRestData): FormABRussianRevise {
            return FormABRussianRevise(
                    productNumber = restData.productNumber,
                    batchNumber = restData.batchNumber,
                    alcoCode = restData.alcoCode,
                    alcoCodeName = restData.alcoCodeName,
                    dateOfPour = restData.dateOfPour,
                    EGAISName = restData.EGAISName,
                    EGAISAddress = restData.EGAISAddress,
                    longName = restData.longName,
                    quantityByTTN = restData.quantityByTTN.toInt(),
                    numberTTN = restData.numberTTN,
                    shipDate = restData.shipDate,
                    isCheck = restData.isCheck.isNotEmpty(),
                    matnrOSN = restData.matnrOSN
            )
        }
    }
}

data class FormABRussianReviseRestData(
        @SerializedName("MATNR")
        val productNumber: String,
        @SerializedName("ZCHARG")
        val batchNumber: String,
        @SerializedName("ZALCOCOD")
        val alcoCode: String,
        @SerializedName("ALCOD_NAME")
        val alcoCodeName: String,
        @SerializedName("DATEOFPOUR")
        val dateOfPour: String,
        @SerializedName("LIFNR_NAME")
        val EGAISName: String,
        @SerializedName("LIFNR_ADR")
        val EGAISAddress: String,
        @SerializedName("ZEXLTEXT")
        val longName: String,
        @SerializedName("ZMENG")
        val quantityByTTN: String,
        @SerializedName("TTN_NUM")
        val numberTTN: String,
        @SerializedName("DATE_SHIP")
        val shipDate: String,
        @SerializedName("FLG_CHECK")
        val isCheck: String,
        @SerializedName("MATNR_OSN")
        val matnrOSN: String) {

    companion object {
        fun from(data: FormABRussianRevise): FormABRussianReviseRestData {
            return FormABRussianReviseRestData(
                    productNumber = data.productNumber,
                    batchNumber = data.batchNumber,
                    alcoCode = data.alcoCode,
                    alcoCodeName = data.alcoCodeName,
                    dateOfPour = data.dateOfPour,
                    EGAISName = data.EGAISName,
                    EGAISAddress = data.EGAISAddress,
                    longName = data.longName,
                    quantityByTTN = data.quantityByTTN.toString(),
                    numberTTN = data.numberTTN,
                    shipDate = data.shipDate,
                    isCheck = if (data.isCheck) "X" else "",
                    matnrOSN = data.matnrOSN
            )
        }
    }
}