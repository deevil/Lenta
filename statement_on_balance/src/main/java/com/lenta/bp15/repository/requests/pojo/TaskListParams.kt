package com.lenta.bp15.repository.requests.pojo

import com.google.gson.annotations.SerializedName

data class TaskListParams(
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** Адресат или номер задания */
        @SerializedName("IV_EXEC_USER")
        val value: String,
        /** Режим обработки: 1 - обновление списка заданий, 2 - расширенный поиск заданий */
        @SerializedName("IV_MODE")
        val mode: Int,
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,
        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val userNumber: String,
        /** Структура для поиска заданий */
        @SerializedName("IS_SEARCH_TASK")
        val searchParams: TaskSearchParams? = null
)