package com.lenta.bp10.mobileObjectsService.models

import com.fasterxml.jackson.annotation.JsonProperty

class MB_S_29(
        /// Аббревиатура типа задания на списание
        @JsonProperty("TASK_TYPE")
        val taskType: String,

        /// Вид движения (управление запасами)
        @JsonProperty("BWART")
        val bwart: String,

        /// Место возникновения затрат
        @JsonProperty("KOSTL")
        val kostl: String,

        /// Склад
        @JsonProperty("LGORTTO")
        val lgortto: String,

        /// Требуется отправка в ГИС
        @JsonProperty("SEND_GIS")
        val sendGis: String,

        /// Признак, что для данного типа задания выбирать причину списания не нужно
        @JsonProperty("NO_GRUND")
        val noGrund: String,

        /// Полное название типа задания на списание
        @JsonProperty("LONG_NAME")
        val longName: String,

        /// Лимит количества товаров в задании
        @JsonProperty("LIMIT")
        val limit: Double,

        /// Используется в производстве
        @JsonProperty("CHK_OWNPR")
        val chkOwnpr: String
)