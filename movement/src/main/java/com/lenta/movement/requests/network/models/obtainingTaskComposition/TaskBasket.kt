package com.lenta.movement.requests.network.models.obtainingTaskComposition

import com.google.gson.annotations.SerializedName

/**"ET_TASK_BASKET"*/
data class TaskBasket(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val materialNumber: String,

        /** Номер корзины */
        @SerializedName("BASKET_NUM")
        val basketNumber: String,

        /** Посчитанное количество */
        @SerializedName("FACT_QNT")
        val quantity: String,

        /** Индикатор: позиция посчитана */
        @SerializedName("XZAEL")
        val positionCounted: String,

        /** Базисная единица измерения */
        @SerializedName("MEINS")
        val uom: String,

        /** Поставщик */
        @SerializedName("LIFNR")
        val supplier: String,

        /** Флаг – «Марочные» остатки */
        @SerializedName("IS_MARK_STOCKS")
        val isExcise: String,

        /** Флаг – «Партионные» остатки */
        @SerializedName("IS_PARTS_STOCKS")
        val isNotExcise: String,

        /** Флаг – «Алкоголь» */
        @SerializedName("IS_ALCO")
        val isAlco: String,

        /** Флаг – «Обычный товар» */
        @SerializedName("IS_USUAL")
        val isUsual: String,

        /** Флаг – «Меркурианский товар» */
        @SerializedName("IS_VET")
        val isVet: String,

        /** Номер партии */
        @SerializedName("ZCHARG")
        val batchNumber: String,

        /** Вид товара */
        @SerializedName("MTART")
        val materialType: String,

        /** Флаг – «Еда» */
        @SerializedName("IS_FOOD")
        val isFood: String
)