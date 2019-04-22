package com.lenta.bp10.models

import com.fasterxml.jackson.annotation.JsonProperty

class WriteOffReasonRestInfo(
        /// Номер товара
        @JsonProperty("MATNR")
        val matnr: String,

        /// Причина движения
        @JsonProperty("GRUND")
        val writeOffCause: String,

        /// Место возникновения затрат
        @JsonProperty("KOSTL")
        val kostl: String,

        /// Введенное количество
        @JsonProperty("FIRST_QNT")
        val amount: String
)