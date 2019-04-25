package com.lenta.bp10.models

import com.fasterxml.jackson.annotation.JsonProperty

class ExciseStampRestInfo(
        /// Номер набора (null для ненабора)
        @JsonProperty("MATNR_OSN")
        val matnrOsn: String,

        /// Номер товара
        @JsonProperty("MATNR")
        val matnr: String,

        /// Причина движения
        @JsonProperty("GRUND")
        val writeOffCause: String,

        /// Код акцизной марки
        @JsonProperty("PDF417")
        val stamp: String,

        /// Признак bad mark
        @JsonProperty("REG")
        val reg: String
)