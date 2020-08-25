package com.lenta.bp16.model.ingredients.results

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.ingredients.MaterialIngredientDataInfo
import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.bp16.model.ingredients.TechOrderDataInfo
import com.lenta.bp16.model.ingredients.OrderByBarcode
import com.lenta.bp16.model.ingredients.ui.IngredientsDataListResultUI
import com.lenta.bp16.platform.converter.IConvertable
import com.lenta.bp16.request.pojo.RetCode
import com.lenta.shared.utilities.extentions.IResultWithRetCodes

data class IngredientsDataListResult(
        /** Данные по заказу */
        @SerializedName("ET_AUFNR_DATA")
        val ordersIngredientsDataInfoList: List<OrderIngredientDataInfo>?,

        /** Данные по материалу */
        @SerializedName("ET_MAT_DATA")
        val materialsIngredientsDataInfoList: List<MaterialIngredientDataInfo>?,

        /** Список заказов по переделу */
        @SerializedName("ET_AUFNR_LIST")
        val techOrdersDataInfoList: List<TechOrderDataInfo>?,

        /**Данные ШК по товарам*/
        @SerializedName("ET_EAN")
        val orderByBarcode: List<OrderByBarcode>?,

        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>?
) : IResultWithRetCodes, IConvertable<IngredientsDataListResultUI?> {

    override fun convert(): IngredientsDataListResultUI? {
        return IngredientsDataListResultUI(
                ordersIngredientsDataInfoList = ordersIngredientsDataInfoList.orEmpty(),
                materialsIngredientsDataInfoList = materialsIngredientsDataInfoList.orEmpty(),
                techOrdersDataInfoList = techOrdersDataInfoList.orEmpty(),
                orderByBarcode = orderByBarcode.orEmpty().mapNotNull { it.convert() },
                retCodes = retCodes.orEmpty()
        )
    }
}