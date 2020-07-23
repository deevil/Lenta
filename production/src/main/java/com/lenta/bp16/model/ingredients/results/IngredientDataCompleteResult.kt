package com.lenta.bp16.model.ingredients.results

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.platform.extention.IResultWithRetCodes
import com.lenta.bp16.request.pojo.DataLabel
import com.lenta.bp16.request.pojo.RetCode

data class IngredientDataCompleteResult(

        /** Признак «Была произведена фиксация на следующем переделе»  */
        @SerializedName("IS_AUTOFIX")
        val isAutofix: Boolean?,

        /** Структура данных для этикетки */
        @SerializedName("ES_DATA_LABEL")
        val dataLabel: DataLabel,
        /** Код тары */
        @SerializedName("EV_CODE_CONT")
        val packCode: String,

        /** SAP код сырья (из позиции новой тары) */
        @SerializedName("MATNR")
        val matnr: String,

        /** Наименование товара сырья */
        @SerializedName("NAME_MATNR")
        val materialName: String,

        /** Наименование готового продукта */
        @SerializedName("NAME_MATNR_DONE")
        val materialNameDone: String,

        /** Условия хранения + срок годности в часах */
        @SerializedName("STOR_COND_TIME")
        val storCondTime: String,

        /** Срок годности */
        @SerializedName("DATE_EXPIR")
        val time: String,

        /** Плановое время окончания этапа (из технологического заказа в часах) */
        @SerializedName("PLAN_AUF_FINISH")
        val planAufFinish: String,

        /** Глобальный номер товара (GTIN) */
        @SerializedName("EAN")
        val ean: String,

        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>
) : IResultWithRetCodes