package com.lenta.bp10.mobileObjectsService.models

import com.fasterxml.jackson.annotation.JsonProperty

// Модель справочника причин товарных движений для типов заданий (WOB)
class MB_S_32(
        /// Тип задания на списание
        @JsonProperty("TASK_TYPE")
        val taskType: String,

        /// Тип контроля задания на списание
        @JsonProperty("TASK_CNTRL")
        val taskCntrl: String,

        /// Причина движения
        @JsonProperty("REASON")
        val reason: String,

        /// Текст к причине движения товара
        @JsonProperty("GRTXT")
        val grtxt: String
)