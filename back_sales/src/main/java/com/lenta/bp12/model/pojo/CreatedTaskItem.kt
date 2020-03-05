package com.lenta.bp12.model.pojo

import com.google.gson.annotations.SerializedName

data class CreatedTaskItem(
        /** Первая строка созданного задания */
        @SerializedName("TEXT1")
        val text1: String,
        /** Вторая строка созданного задания */
        @SerializedName("TEXT2")
        val text2: String
)