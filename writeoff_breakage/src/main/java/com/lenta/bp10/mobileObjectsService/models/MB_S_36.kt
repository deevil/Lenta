package com.lenta.bp10.mobileObjectsService.models

import com.fasterxml.jackson.annotation.JsonProperty

// Модель справочника настроек заданий на списание WOB (справочник текстов ГИС-контроля)
class MB_S_36(
        /// Тип контроля задания на списание
        @JsonProperty("TASK_CNTRL")
        val taskCntrl: String,

        /// Текст длиной 40 знаков
        @JsonProperty("CNTRL_TXT")
        val cntrlTxt: String
)