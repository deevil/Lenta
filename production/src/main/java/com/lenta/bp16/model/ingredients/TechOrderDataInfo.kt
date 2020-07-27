package com.lenta.bp16.model.ingredients

import com.google.gson.annotations.SerializedName

/**
 * Список заказов по переделу
 */
data class TechOrderDataInfo(
        /** Код передела (ключ образца) */
        @SerializedName("KTSCH")
        val ktsch: String?,

        /** Текст первой строки (заказ) */
        @SerializedName("TEXT1")
        val text1: String?,

        /** Текст второй строки (итоговый ПФ) */
        @SerializedName("TEXT2")
        val text2: String?,

        /** Текст третьей строки (заголовка экрана – наименование передела и склад) */
        @SerializedName("TEXT3")
        val text3: String?,

        /** Плановое количество ингредиента в заказе */
        @SerializedName("PLAN_QNT")
        val plan_qnt: String?
)