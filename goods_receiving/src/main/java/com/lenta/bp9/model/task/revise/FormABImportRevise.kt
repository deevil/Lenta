package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName
import com.lenta.shared.utilities.extentions.toStringFormatted

//ET_FORMA_IMP - Таблица Справок А и Б (Импортный товар)
data class FormABImportRevise(
        val productNumber: String, // Номер товара
        val batchNumber: String, // Номер партии
        val alcoCode: String, // Номер товара в ЕГАИС (АлкоКод)
        val EGAISName: String, // Наименование ( ЕГАИС )
        val EGAISAddress: String, // Адрес ( ЕГАИС )
        val numberTTN_A: String, // Номер ТТН/Номер ГТД
        val numberTTN_B: String, // Номер ТТН/Номер ГТД
        val longName_A: String, // Длинное наименование
        val longName_B: String, // Длинное наименование
        val date_A: String, // ??? - Дата
        val date_B: String, // ??? - Дата
        val quantityByTTN_B: Double, // Количество по ТТН/ГТД
        val EGAISFixDocumentNumber_B: String, // Номер документа фиксации ЕГАИС
        val EGAISFixDate_B: String, // Дата фиксации в ЕГАИС
        val lastB: Int, // ??? - Двухбайтовое целое число (со знаком)
        val isCheck: Boolean, // ??? - Общий флаг
        val matnrOSN: String // ??? - Номер товара
) {

    companion object {
        fun from(restData: FormABImportReviseRestData): FormABImportRevise {
            return FormABImportRevise(
                    productNumber = restData.productNumber,
                    batchNumber = restData.batchNumber,
                    alcoCode = restData.alcoCode,
                    EGAISName = restData.EGAISName,
                    EGAISAddress = restData.EGAISAddress,
                    numberTTN_A = restData.numberTTN_A,
                    numberTTN_B = restData.numberTTN_B,
                    longName_A = restData.longName_A,
                    longName_B = restData.longName_B,
                    date_A = restData.date_A,
                    date_B = restData.date_B,
                    quantityByTTN_B = restData.quantityByTTN_B.toDouble(),
                    EGAISFixDocumentNumber_B = restData.EGAISFixDocumentNumber_B,
                    EGAISFixDate_B = restData.EGAISFixDate_B,
                    lastB = restData.lastB.trim().toInt(),
                    isCheck = restData.isCheck.isNotEmpty(),
                    matnrOSN = restData.matnrOSN
            )
        }
    }
}

data class FormABImportReviseRestData(
        @SerializedName("MATNR")
        val productNumber: String,
        @SerializedName("ZCHARG")
        val batchNumber: String,
        @SerializedName("ZALCOCOD")
        val alcoCode: String,
        @SerializedName("LIFNR_NAME")
        val EGAISName: String,
        @SerializedName("LIFNR_ADR")
        val EGAISAddress: String,
        @SerializedName("NUM_GTD_A")
        val numberTTN_A: String,
        @SerializedName("NUM_GTD_B")
        val numberTTN_B: String,
        @SerializedName("NAME_A")
        val longName_A: String,
        @SerializedName("NAME_B")
        val longName_B: String,
        @SerializedName("DATE_IN_A")
        val date_A: String,
        @SerializedName("DATE_IN_B")
        val date_B: String,
        @SerializedName("ZMENGE_B")
        val quantityByTTN_B: String,
        @SerializedName("EGFIXN_B")
        val EGAISFixDocumentNumber_B: String,
        @SerializedName("EGFIXD_B")
        val EGAISFixDate_B: String,
        @SerializedName("LASTB")
        val lastB: String,
        @SerializedName("FLG_CHECK")
        val isCheck: String,
        @SerializedName("MATNR_OSN")
        val matnrOSN: String) {

    companion object {
        fun from(data: FormABImportRevise): FormABImportReviseRestData {
            return FormABImportReviseRestData(
                    productNumber = data.productNumber,
                    batchNumber = data.batchNumber,
                    alcoCode = data.alcoCode,
                    EGAISName = data.EGAISName,
                    EGAISAddress = data.EGAISAddress,
                    numberTTN_A = data.numberTTN_A,
                    numberTTN_B = data.numberTTN_B,
                    longName_A = data.longName_A,
                    longName_B = data.longName_B,
                    date_A = data.date_A,
                    date_B = data.date_B,
                    quantityByTTN_B = data.quantityByTTN_B.toStringFormatted(),
                    EGAISFixDocumentNumber_B = data.EGAISFixDocumentNumber_B,
                    EGAISFixDate_B = data.EGAISFixDate_B,
                    lastB = data.lastB.toString(),
                    isCheck = if (data.isCheck) "X" else "",
                    matnrOSN = data.matnrOSN
            )
        }
    }
}