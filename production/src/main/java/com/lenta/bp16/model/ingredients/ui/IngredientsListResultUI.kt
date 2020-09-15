package com.lenta.bp16.model.ingredients.ui

import com.lenta.bp16.request.pojo.RetCode
import com.lenta.shared.utilities.extentions.IResultWithRetCodes

data class IngredientsListResultUI(
        /** список всех ингредиентов по заказам и по материалам */
        val ingredientsList: List<IngredientInfoUI>?,
        /**Список технических заказов*/
        val goodsListByOrder: List<GoodByOrderUI>?,
        /**Список ШК*/
        val goodsEanList: List<OrderByBarcodeUI>?,
        /** Таблица возврата */
        override val retCodes: List<RetCode>?
) : IResultWithRetCodes