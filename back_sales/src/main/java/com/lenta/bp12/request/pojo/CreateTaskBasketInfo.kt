package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class CreateTaskBasketInfo(
        /** Номер корзины */
        @SerializedName("BASKET_NUM")
        var basketNumber: String?,
        /** Обычный товар */
        @SerializedName("IS_USUAL")
        var isCommon: String?,
        /** Алкогольный товар */
        @SerializedName("IS_ALCO")
        var isAlcohol: String?,
        /** Маркированный товар */
        @SerializedName("IS_MARK")
        var isMark: String? = "",
        /** Меркурианский товар */
        @SerializedName("IS_VET")
        var isVet: String = "",
        /** Код поставщика */
        @SerializedName("LIFNR")
        var providerCode: String?,
        /** Вид товара */
        @SerializedName("MTART")
        var goodType: String?,
        /** Номер секции */
        @SerializedName("ABTNR")
        var section: String?,
        /** Группа закупок */
        @SerializedName("EKGRP")
        var purchaseGroup: String? = "",
        /** Группа маркировки для деления задания */
        @SerializedName("MARKTYPE_GROUP")
        var marktypeGroup: String? = "",
        /** Группа МРЦ для товара */
        @SerializedName("GROUP_MPR")
        var groupMpr: String? = "",
        /** Паллетная ведомость по корзинке напечатана */
        @SerializedName("IS_PRINT")
        var isPrint: String? = "",
        /** Корзинка закрыта для добавления товаров */
        @SerializedName("IS_CLOSE")
        var isClose: String? = ""
)