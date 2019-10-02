package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName
import com.lenta.shared.utilities.extentions.toStringFormatted

//ET_VBELN_POS - Структура сверки накладной
data class InvoiceRevise(
        val numberTTN: String, // Номер ТТН/Номер ГТД
        val dateTTN: String, // Дата
        val supplierAccountNumber: String, //Номер счета поставщика или кредитора
        val supplierName: String, // УТЗ ТСД, Название поставщика
        val supplierINN: String, // Идентификационный номер налогоплательщика
        val supplierAddress: String, // Адрес поставщика
        val quantityPositions: Int, // ??? - Натуральное число
        val quantityST: Int, // ??? - Натуральное число
        val quantityKG: Double, // ??? - Количество по ТТН
        val quantityAll: Double, // ??? - Количество по ТТН
        val quantityString: String, // ??? - Текст 30 знаков
        val quantityUOM: Double, // ??? - Количество по ТТН
        val isEDO: Boolean // ??? - Общий флаг
) {

    companion object {
        fun from(restData: InvoiceReviseRestData): InvoiceRevise {
            return InvoiceRevise(
                    numberTTN = restData.numberTTN,
                    dateTTN = restData.dateTTN,
                    supplierAccountNumber = restData.supplierAccountNumber,
                    supplierName = restData.supplierName,
                    supplierINN = restData.supplierINN,
                    supplierAddress = restData.supplierAddress,
                    quantityPositions = restData.quantityPositions.toInt(),
                    quantityST = restData.quantityST.toInt(),
                    quantityKG = restData.quantityKG.toDouble(),
                    quantityAll = restData.quantityAll.toDouble(),
                    quantityString = restData.quantityString,
                    quantityUOM = restData.quantityUOM.toDouble(),
                    isEDO = restData.isEDO.isNotEmpty()
            )
        }
    }
}

data class InvoiceReviseRestData(
        @SerializedName("TTN_NUM")
        val numberTTN: String,
        @SerializedName("TTN_DATE")
        val dateTTN: String,
        @SerializedName("LIFNR")
        val supplierAccountNumber: String,
        @SerializedName("LIFNR_NAME")
        val supplierName: String,
        @SerializedName("STCD1")
        val supplierINN: String,
        @SerializedName("LIFNR_ADR")
        val supplierAddress: String,
        @SerializedName("QNT_POS")
        val quantityPositions: String,
        @SerializedName("QNT_ST")
        val quantityST: String,
        @SerializedName("QNT_KG")
        val quantityKG: String,
        @SerializedName("QNT_ALL")
        val quantityAll: String,
        @SerializedName("QNT_STR")
        val quantityString: String,
        @SerializedName("QNT_UOM")
        val quantityUOM: String,
        @SerializedName("IS_EDO")
        val isEDO: String
) {

    companion object {
        fun from(data: InvoiceRevise): InvoiceReviseRestData {
            return InvoiceReviseRestData(
                    numberTTN = data.numberTTN,
                    dateTTN = data.dateTTN,
                    supplierAccountNumber = data.supplierAccountNumber,
                    supplierName = data.supplierName,
                    supplierINN = data.supplierINN,
                    supplierAddress = data.supplierAddress,
                    quantityPositions = data.quantityPositions.toString(),
                    quantityST = data.quantityST.toString(),
                    quantityKG = data.quantityKG.toStringFormatted(),
                    quantityAll = data.quantityAll.toStringFormatted(),
                    quantityString = data.quantityString,
                    quantityUOM = data.quantityUOM.toStringFormatted(),
                    isEDO = if (data.isEDO) "X" else ""
            )
        }
    }
}