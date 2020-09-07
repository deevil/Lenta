package com.lenta.bp16.model.ingredients.results

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.ProducerDataInfo
import com.lenta.bp16.model.ingredients.*
import com.lenta.bp16.model.ingredients.ui.IngredientsDataListResultUI
import com.lenta.bp16.model.ZPartDataInfo
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

        /** Данные по меркурианским партиям */
        @SerializedName("ET_VET_PARTS")
        val mercuryPartDataInfoList: List<MercuryPartDataInfo>?,

        /** Данные по Z-партиям */
        @SerializedName("ET_Z_PARTS")
        val zPartDataInfoList: List<ZPartDataInfo>?,

        /** Справочник производителей */
        @SerializedName("ET_PROD_TEXT")
        val producerDataInfoList: List<ProducerDataInfo>?,

        /** Список заказов по переделу */
        @SerializedName("ET_AUFNR_LIST")
        val techOrdersDataInfoList: List<TechOrderDataInfo>?,

        /** Данные ШК по товарам */
        @SerializedName("ET_EAN")
        val orderByBarcode: List<OrderByBarcode>?,

        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>?
) : IResultWithRetCodes, IConvertable<IngredientsDataListResultUI> {

    override fun convert(): IngredientsDataListResultUI {
        return IngredientsDataListResultUI(
                ordersIngredientsDataInfoList = ordersIngredientsDataInfoList.orEmpty(),
                materialsIngredientsDataInfoList = materialsIngredientsDataInfoList.orEmpty(),
                techOrdersDataInfoList = techOrdersDataInfoList.orEmpty(),
                mercuryPartDataInfoList = mercuryPartDataInfoList.orEmpty(),
                producerDataInfoList = producerDataInfoList.orEmpty(),
                zPartDataInfoList = zPartDataInfoList.orEmpty(),
                orderByBarcode = orderByBarcode.orEmpty().mapNotNull { it.convert() },
                retCodes = retCodes.orEmpty()
        )
    }
}