package com.lenta.bp10.mobileObjectsService.models

import com.fasterxml.jackson.annotation.JsonProperty

// Модель справочника закрепления причин списания брака за секциями (WOB)
class MB_S_31(
        /// Тип задания на списание
        @JsonProperty("TASK_TYPE")
        val taskType: String,

        /// Номер отдела
        @JsonProperty("SECTION_ID")
        val sectionId: String,

        /// Причина движения
        @JsonProperty("REASON")
        val reason: String,

        /// Текст к причине движения товара
        @JsonProperty("GRTXT")
        val grtxt: String
)