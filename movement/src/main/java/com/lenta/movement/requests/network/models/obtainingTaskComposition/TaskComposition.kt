package com.lenta.movement.requests.network.models.obtainingTaskComposition

import com.google.gson.annotations.SerializedName

/**"ET_TASK_POS"*/
data class TaskComposition(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val materialNumber: String,

        /** Индикатор: Позиция  посчитана*/
        @SerializedName("XZAEL")
        val positionCounted: String,

        /** Количество вложенного */
        @SerializedName("QNTINCL")
        val quantityInvestments: String,

        /** Единица измерения заказа на поставку */
        @SerializedName("BSTME")
        val orderUnits: String,

        /** Индикатор: Товар «Еда» */
        @SerializedName("IS_FOOD")
        val isFood: String,

        /** Объем заказа */
        @SerializedName("MENGE")
        val quantity: String,

        /** Рекомендуемая дата с */
        @SerializedName("REQ_DATE_FROM")
        val recommendedDateFrom: String,

        /** Рекомендуемая дата по */
        @SerializedName("REQ_DATE_TO")
        val recommendedDateTO: String,

        /** Объем БЕИ */
        @SerializedName("VOLUM")
        val volume: String
)