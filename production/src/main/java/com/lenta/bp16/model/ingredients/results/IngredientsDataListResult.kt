package com.lenta.bp16.model.ingredients.results

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.ingredients.MaterialIngredientDataInfo
import com.lenta.bp16.model.ingredients.OrderByRemake
import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.bp16.model.ingredients.ui.OrderByBarcode
import com.lenta.bp16.platform.extention.IResultWithRetCodes
import com.lenta.bp16.request.pojo.RetCode

data class IngredientsDataListResult(
        /** Данные по заказу */
        @SerializedName("ET_AUFNR_DATA")
        val ordersIngredientsDataInfoList: List<OrderIngredientDataInfo>?,

        /** Данные по материалу */
        @SerializedName("ET_MAT_DATA")
        val materialsIngredientsDataInfoList: List<MaterialIngredientDataInfo>?,

        /** Список заказов по переделу */
        @SerializedName("ET_AUFNR_LIST")
        val ordersByRemakeList: List<OrderByRemake>?,

        @SerializedName("ET_EAN")
        val orderByBarcode: List<OrderByBarcode>?,

        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>
) : IResultWithRetCodes