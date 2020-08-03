package com.lenta.movement.requests.network.models.obtainingDataExciseGoodsNetRequest

import com.google.gson.annotations.SerializedName

data class StampsBox(
        @SerializedName("BOX_NUM")
        val boxNumber: String?, //Номер коробки
        @SerializedName("MARK_NUM")
        val exciseStampCode: String? //Код акцизной марки
)