package com.lenta.bp16.model.ingredients.ui

data class TechOrderDataInfoUI (
        /** Код передела (ключ образца) */
        val ktsch: String,
        /** Текст первой строки (заказ) */
        val text1: String,
        /** Текст второй строки (итоговый ПФ) */
        val text2: String,
        /** Текст третьей строки (заголовка экрана – наименование передела и склад) */
        val text3: String,
        /** Плановое количество ингредиента в заказе */
        val plan_qnt: String
)