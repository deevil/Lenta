package com.lenta.bp10.fmp.resources.send_report

import com.google.gson.annotations.SerializedName

data class WriteOffReport(

        // <summary>
        // Табельный номер
        // </summary>
        @SerializedName("IV_PERNR")
        val perNo: String,

        // <summary>
        // Принтер
        // </summary>
        @SerializedName("IV_PRINTERNAME")
        val printer: String,

        // <summary>
        // Название задания
        // </summary>
        @SerializedName("IV_DESCR")
        val taskName: String,

        // <summary>
        // Тип задания на списание
        // </summary>
        @SerializedName("IV_TYPE")
        val taskType: String,

        // <summary>
        // Предп
        // </summary>
        @SerializedName("IV_WERKS")
        val tkNumber: String,

        // <summary>
        // Склад
        // </summary>
        @SerializedName("IV_LGORT")
        val storloc: String,

        // <summary>
        // IP адрес ТСД
        // </summary>
        @SerializedName("IV_IP")
        val ipAdress: String,

        // <summary>
        // Список товаров для сохранения задания из ТСД
        // </summary>
        @SerializedName("IT_MATERIALS")
        val materials: List<MaterialNumber>,

        // <summary>
        // Список марок для сохранения задания из ТСД
        // </summary>
        @SerializedName("IT_MARKS")
        val exciseStamps: List<ExciseStamp>


)

data class MaterialNumber(
        // <summary>
        // Номер товара
        // </summary>
        @SerializedName("MATNR")
        val matnr: String,

        // <summary>
        // Причина движения
        // </summary>
        @SerializedName("GRUND")
        val writeOffCause: String,

        // <summary>
        // Место возникновения затрат
        // </summary>
        @SerializedName("KOSTL")
        val kostl: String,

        // <summary>
        // Введенное количество
        // </summary>
        @SerializedName("FIRST_QNT")
        val amount: String

)

// Акцизная марка, элемент списка IT_MARKS в ZMP_UTZ_WOB_04_V001
// </summary>
data class ExciseStamp(
        // <summary>
        // Номер набора (null для ненабора)
        // </summary>
        @SerializedName("MATNR_OSN")
        val matnrOsn: String?,

        // <summary>
        // Номер товара
        // </summary>
        @SerializedName("MATNR")
        val matnr: String,

        // <summary>
        // Причина движения
        // </summary>
        @SerializedName("GRUND")
        val writeOffCause: String,

        // <summary>
        // Код акцизной марки
        // </summary>
        @SerializedName("PDF417")
        val stamp: String,

        // <summary>
        // Признак bad mark
        // </summary>
        @SerializedName("REG")
        val reg: String

)