package com.lenta.bp10.mobileObjectsService.models

import com.fasterxml.jackson.annotation.JsonProperty

class MB_S_35(
        /// Тип задания на списание
        @JsonProperty("TASK_TYPE")
        val taskType: String,

        /// Тип контроля задания на списание
        @JsonProperty("TASK_CNTRL")
        val taskCntrl: String
)