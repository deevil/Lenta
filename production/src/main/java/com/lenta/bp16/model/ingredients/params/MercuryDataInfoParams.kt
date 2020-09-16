package com.lenta.bp16.model.ingredients.params

import com.google.gson.annotations.SerializedName


data class MercuryDataInfoParams (

        /** Номер предприятия */
        @SerializedName("WERKS")
        val tkNumber: String,

        /** Номер ЕО */
        @SerializedName("EXIDV")
        val numberEO: String,

        /** Номер материала */
        @SerializedName("MATNR")
        val matnr: String
)