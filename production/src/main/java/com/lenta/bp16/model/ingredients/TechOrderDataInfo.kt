package com.lenta.bp16.model.ingredients

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.ingredients.ui.TechOrderDataInfoUI
import com.lenta.bp16.platform.converter.IConvertable

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
) : IConvertable<TechOrderDataInfoUI?> {
    override fun convert(): TechOrderDataInfoUI? {
        return TechOrderDataInfoUI(
                ktsch = ktsch.orEmpty(),
                text1 = text1.orEmpty(),
                text2 = text2.orEmpty(),
                text3 = text3.orEmpty(),
                plan_qnt = plan_qnt.orEmpty()
        )
    }
}