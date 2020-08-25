package com.lenta.bp16.model.ingredients.ui

import com.lenta.bp16.model.ingredients.GoodByOrder
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.OrderByBarcode
import com.lenta.bp16.request.pojo.RetCode
import com.lenta.shared.utilities.extentions.IResultWithRetCodes

data class IngredientsListResultUI (
        /** список всех ингредиентов по заказам и по материалам */
        val ingredientsList: List<IngredientInfo>?,

        /**Список технических заказов*/
        val goodsListByOrder: List<GoodByOrder>?,

        /**Список ШК*/
        val goodsEanList: List<OrderByBarcode>?,

        /** Таблица возврата */
        override val retCodes: List<RetCode>?
): IResultWithRetCodes