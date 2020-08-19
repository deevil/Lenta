package com.lenta.bp16.model.ingredients.params

import com.google.gson.annotations.SerializedName

data class GetIngredientDataParams(

        /**
         * Код предприятия
         */
        @SerializedName("IV_WERKS")
        val tkMarket: String, //

        /**
         * Ip адрес ТСД
         */
        @SerializedName("IV_IP")
        val deviceIP: String,

        /**
         * Номер объекта
         */
        @SerializedName("IV_OBJ_CODE ")
        val code: String,

        @SerializedName("IT_LGORTS")
        val warehouse: List<WarehouseParam>,

        /**
        Режим обработки:
        5 – Получение данных с блокировкой заказа
        6 – Получение данных с переблокировкой заказа
        7 – Получение данных с блокировкой материала
        8 – Получение данных с переблокировкой материала
         */
        @SerializedName("IV_MODE")
        val mode: String,

        /**
         * Количество ПФ, на которое будет производиться комплектация ингредиентов
         */
        @SerializedName("IV_FACT_QNT")
        val weight: String
)