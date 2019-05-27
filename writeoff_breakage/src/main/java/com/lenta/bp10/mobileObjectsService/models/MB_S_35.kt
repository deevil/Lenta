package com.lenta.bp10.mobileObjectsService.models

import com.google.gson.annotations.SerializedName

class MB_S_35(
        /// Тип задания на списание
        @SerializedName("TASK_TYPE")
        val taskType: String,

        /// Тип контроля задания на списание
        @SerializedName("TASK_CNTRL")
        val taskCntrl: String
)