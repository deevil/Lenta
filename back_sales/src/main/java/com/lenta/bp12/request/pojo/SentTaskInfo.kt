package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class SentTaskInfo(
        /** Первая строка созданного задания */
        @SerializedName("TEXT1")
        val text1: String,
        /** Вторая строка созданного задания */
        @SerializedName("TEXT2")
        val text2: String
)