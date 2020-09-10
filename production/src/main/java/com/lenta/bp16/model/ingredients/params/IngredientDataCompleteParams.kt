package com.lenta.bp16.model.ingredients.params

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.BatchNewDataInfoParam

data class IngredientDataCompleteParams(
        /** Код предприятия */
        @SerializedName("IV_WERKS")
        val tkMarket: String,

        /** Ip адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIP: String,

        /** Ключ образца */
        @SerializedName("IV_KTSCH")
        val ktsch: String? = null, //Не используется в ZTM_UTZ_PRO_04_V001 интерфейсе, но необходим в ZTM_UTZ_PRO_12_V001 интерфейсе

        /**
        Тип родительской связи для создания тары:
        1 – ЕО
        2 – ВП
        3 - Тара
         */
        @SerializedName("IV_MODE")
        val mode: String,

        /** Номер родительской связи */
        @SerializedName("IV_PARENT")
        val parent: String,

        /** Номер технологического заказа */
        @SerializedName("IV_AUFNR")
        val aufnr: String,

        /** SAP – код товара */
        @SerializedName("IV_MATNR")
        val matnr: String,

        /** Фактическое количество сырья */
        @SerializedName("IV_FACT_QNT")
        val fact: Double,

        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val personnelNumber: String,

        /** Номер ЗСЖ */
        @SerializedName("IV_ENTRYID")
        val entryId: String,

        /** Номер Z-партии */
        @SerializedName("IV_BATCHID")
        val batchId: String,

        /** Таблица партионных признаков для создания новой партии */
        val batchNewParam : List<BatchNewDataInfoParam>

) {
        companion object {
                const val MODE_INGREDIENT = "4"
                const val MODE_MATERIAL = "5"
        }
}