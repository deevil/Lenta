package com.lenta.bp16.model.ingredients.params

import com.google.gson.annotations.SerializedName

data class IngredientDataCompleteParams(
        /** Код предприятия */
        @SerializedName("IV_WERKS")
        val tkMarket: String,

        /** Ip адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIP: String,

        /**
        Тип родительской связи для создания тары:
        1 – ЕО
        2 – ВП
        3 - Тара
         */
        @SerializedName("IV_MODE")
        val mode: String = "",

        /** Номер родительской связи  */
        @SerializedName("IV_PARENT")
        val parent: String,

        /** SAP – код товара  */
        @SerializedName("IV_MATNR")
        val matnr: String,

        /** Фактическое количество сырья  */
        @SerializedName("IV_FACT_QNT")
        val fact: Double,

        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val personnelNumber: String
) {
        companion object {
                const val MODE_INGREDIENT = "4"
        }
}