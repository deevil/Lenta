package com.lenta.bp10.mobileObjectsService.models

import com.fasterxml.jackson.annotation.JsonProperty

// модель справочника принтеров
class MB_S_26(
        /// Номер ТК
        @JsonProperty("WERKS")
        val tkNumber: String,

        /// Наименование принтера
        @JsonProperty("PRINTERNAME")
        val printerName: String,

        /// Информация о принтере
        @JsonProperty("PRINTERINFO")
        val printerInfo: String
)