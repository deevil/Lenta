package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class TaskInfo(
        /** Номер объекта */
        @SerializedName("OBJ_CODE")
        var number: String,
        /** Тип блокировки: 1 - своя, 2 - чужая */
        @SerializedName("BLOCK_TYPE")
        var blockType: String,
        /** Имя пользователя */
        @SerializedName("LOCK_USER")
        var lockUser: String,
        /** IP адрес ТСД */
        @SerializedName("LOCK_IP")
        var lockIp: String,
        /** Общий флаг */
        @SerializedName("IS_PLAY")
        var isPlay: String,
        /** Индикатор – товар на упаковку */
        @SerializedName("IS_PACK")
        var isPack: String,
        /** Кол-во позиций */
        @SerializedName("QNT_POS")
        var quantity: String,
        /** Текст первой строки */
        @SerializedName("TEXT1")
        var text1: String,
        /** Текст второй строки */
        @SerializedName("TEXT2")
        var text2: String,
        /** Текст третьей строки */
        @SerializedName("TEXT3")
        var text3: String
)











