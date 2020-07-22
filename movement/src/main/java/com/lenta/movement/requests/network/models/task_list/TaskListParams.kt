package com.lenta.movement.requests.network.models.task_list

import com.google.gson.annotations.SerializedName

data class TaskListParams(
        /** TK */
        @SerializedName("IV_WERKS")
        val tkNumber: String,

        /** Адресат */
        @SerializedName("IV_EXEC_USER")
        val user: String,

        /**
         * режим работы ФМ: 1 - обновление, 2 - расширенный поиск
         */
        @SerializedName("IV_MODE")
        val mode: String,

        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val personellNumber : String,

        @SerializedName("IS_SEARCH_TASK")
        val filter: SearchTaskFilter?
)