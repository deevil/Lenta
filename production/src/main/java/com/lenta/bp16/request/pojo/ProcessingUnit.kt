package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class ProcessingUnit(
        /** Номер ЕО */
        @SerializedName("EXIDV")
        var number: String,
        /** Тип блокировки: 1 - своя, 2 - чужая */
        @SerializedName("BLOCK_TYPE")
        var blockType: Int,
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
        var quantity: Int
)