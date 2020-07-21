package com.lenta.bp16.model.ingredients

import com.google.gson.annotations.SerializedName

//Список товаров по определенным заказам
data class GoodByOrder(
        /** Номер технологического заказа */
        @SerializedName("AUFNR")
        val aufnr: String,
        /** SAP-код ингредиента */
        @SerializedName("MATNR")
        val matnr: String
)