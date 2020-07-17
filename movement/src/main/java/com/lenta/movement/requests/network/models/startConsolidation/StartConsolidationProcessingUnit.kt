package com.lenta.movement.requests.network.models.startConsolidation

import com.google.gson.annotations.SerializedName

/** ET_TASK_EXIDV Список ЕО*/
data class StartConsolidationProcessingUnit(
        /** Номер ЕО */
        @SerializedName("EXIDV")
        val processingUnitNumber: String,

        /** Номер корзины */
        @SerializedName("BASKET_NUM")
        val basketNumber: String,

        /** Поставщик */
        @SerializedName("LIFNR")
        val supplier: String,

        /** Флаг – «Алкоголь» */
        @SerializedName("IS_ALCO")
        val isAlco: String,

        /** Флаг – «Обычный товар» */
        @SerializedName("IS_USUAL")
        val isUsual: String,

        /** Количество позиций */
        @SerializedName("QNT_SKU")
        val quantity: String
)