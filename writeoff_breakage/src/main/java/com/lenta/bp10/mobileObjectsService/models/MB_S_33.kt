package com.lenta.bp10.mobileObjectsService.models

import com.fasterxml.jackson.annotation.JsonProperty

// Модель справочника настроек заданий на списание WOB (справочник складов)
class MB_S_33(
        /// Тип задания на списание
        @JsonProperty("TASK_TYPE")
        val taskType: String,

        /// ТК
        @JsonProperty("WERKS")
        val werks: String,

        /// Склад
        @JsonProperty("LGORT")
        val lgort: String
)