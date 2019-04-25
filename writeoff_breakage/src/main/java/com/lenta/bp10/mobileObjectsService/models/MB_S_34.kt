package com.lenta.bp10.mobileObjectsService.models

import com.fasterxml.jackson.annotation.JsonProperty

// Модель справочника настроек заданий на списание WOB (справочник видов товара)
class MB_S_34(/// Тип задания на списание
        @JsonProperty("TASK_TYPE")
        val taskType: String,

        /// Вид товара
        @JsonProperty("MTART")
        val mtart: String
)