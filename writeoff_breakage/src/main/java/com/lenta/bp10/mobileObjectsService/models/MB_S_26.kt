package com.lenta.bp10.mobileObjectsService.models

import com.google.gson.annotations.SerializedName

// модель справочника принтеров
class MB_S_26(
        /// Номер ТК
        @SerializedName("WERKS")
        val tkNumber: String,

        /// Наименование принтера
        @SerializedName("PRINTERNAME")
        val printerName: String,

        /// Информация о принтере
        @SerializedName("PRINTERINFO")
        val printerInfo: String
)