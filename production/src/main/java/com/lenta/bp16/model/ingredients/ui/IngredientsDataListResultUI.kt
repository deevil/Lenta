package com.lenta.bp16.model.ingredients.ui

import com.lenta.bp16.model.ingredients.MaterialIngredientDataInfo
import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.bp16.model.ingredients.TechOrderDataInfo
import com.lenta.bp16.request.pojo.RetCode
import com.lenta.shared.utilities.extentions.IResultWithRetCodes

data class IngredientsDataListResultUI(
        /** Данные по заказу */
        val ordersIngredientsDataInfoList: List<OrderIngredientDataInfo>?,

        /** Данные по материалу */
        val materialsIngredientsDataInfoList: List<MaterialIngredientDataInfo>?,

        /** Список заказов по переделу */
        val techOrdersDataInfoList: List<TechOrderDataInfo>?,

        /**Данные ШК по товарам*/
        val orderByBarcode: List<OrderByBarcodeUI>?,

        /** Таблица возврата */
        override val retCodes: List<RetCode>?
) : IResultWithRetCodes