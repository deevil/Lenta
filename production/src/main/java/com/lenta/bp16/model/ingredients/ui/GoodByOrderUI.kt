package com.lenta.bp16.model.ingredients.ui

//Список товаров по определенным заказам
data class GoodByOrderUI (
    /** Номер технологического заказа */
    val aufnr: String,
    /** SAP-код ингредиента */
    val matnr: String
)