package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

/**
 * ZMP_UTZ_BKS_03_V001
 * "Получение состава задания"
 * @see com.lenta.bp12.request.TaskContentNetRequest
 **/

data class CreateTaskBasketInfo(
        /** Номер корзины */
        @SerializedName("BASKET_NUM")
        val basketNumber: String?,
        /** Обычный товар */
        @SerializedName("IS_USUAL")
        val isCommon: String?,
        /** Алкогольный товар */
        @SerializedName("IS_ALCO")
        val isAlcohol: String?,
        /** Маркированный товар */
        @SerializedName("IS_MARK")
        val isMark: String? = "",
        /** Меркурианский товар */
        @SerializedName("IS_VET")
        val isVet: String? = "",
        /** Код поставщика */
        @SerializedName("LIFNR")
        val providerCode: String?,
        /** Вид товара */
        @SerializedName("MTART")
        val goodType: String?,
        /** Номер секции */
        @SerializedName("ABTNR")
        val section: String?,
        /** Группа закупок */
        @SerializedName("EKGRP")
        val purchaseGroup: String? = "",
        /** Группа маркировки для деления задания */
        @SerializedName("MARKTYPE_GROUP")
        val marktypeGroup: String? = "",
        /** Группа МРЦ для товара */
        @SerializedName("GROUP_MPR")
        val groupMpr: String? = "",
        /** Паллетная ведомость по корзинке напечатана */
        @SerializedName("IS_PRINT")
        val isPrint: String? = "",
        /** Корзинка закрыта для добавления товаров */
        @SerializedName("IS_CLOSE")
        val isClose: String? = ""
) : ITaskBasketInfo

interface ITaskBasketInfo