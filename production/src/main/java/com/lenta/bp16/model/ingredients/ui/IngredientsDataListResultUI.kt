package com.lenta.bp16.model.ingredients.ui

import com.lenta.bp16.request.pojo.RetCode
import com.lenta.shared.utilities.extentions.IResultWithRetCodes

data class IngredientsDataListResultUI(
        /** Данные по заказу */
        val ordersIngredientsDataInfoList: List<OrderIngredientDataInfoUI>,
        /** Данные по материалу */
        val materialsIngredientsDataInfoList: List<MaterialIngredientDataInfoUI>,
        /** Данные по меркурианским партиям */
        val mercuryPartDataInfoList: List<MercuryPartDataInfoUI>,
        /** Данные по Z-партиям */
        val zPartDataInfoList: List<ZPartDataInfoUI>,
        /** Справочник производителей */
        val producerDataInfoList: List<ProducerDataInfoUI>,
        /** Список заказов по переделу */
        val techOrdersDataInfoList: List<TechOrderDataInfoUI>,
        /**Данные ШК по товарам*/
        val orderByBarcode: List<OrderByBarcodeUI>,
        /** Таблица возврата */
        override val retCodes: List<RetCode>
) : IResultWithRetCodes