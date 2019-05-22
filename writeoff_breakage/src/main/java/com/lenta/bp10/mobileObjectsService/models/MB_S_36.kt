package com.lenta.bp10.mobileObjectsService.models

import com.google.gson.annotations.SerializedName

// Модель справочника настроек заданий на списание WOB (справочник текстов ГИС-контроля)
class MB_S_36(
        /// Тип контроля задания на списание
        @SerializedName("TASK_CNTRL")
        val taskCntrl: String,

        /// Текст длиной 40 знаков
        @SerializedName("CNTRL_TXT")
        val cntrlTxt: String
)