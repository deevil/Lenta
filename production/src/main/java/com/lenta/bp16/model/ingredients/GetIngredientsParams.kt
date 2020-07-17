package com.lenta.bp16.model.ingredients

import com.google.gson.annotations.SerializedName


data class GetIngredientsParams(
        @SerializedName("IV_WERKS")
        val tkMarket: String, // Код предприятия
        @SerializedName("IV_IP")
        val deviceIP: String, //Ip адрес ТСД
        @SerializedName("IT_LGORT_LIST") // Список складов = LGORT
        val workhousesList: List<String>
)