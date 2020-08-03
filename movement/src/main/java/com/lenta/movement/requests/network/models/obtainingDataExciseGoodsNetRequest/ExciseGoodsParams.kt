package com.lenta.movement.requests.network.models.obtainingDataExciseGoodsNetRequest

import com.google.gson.annotations.SerializedName

data class ExciseGoodsParams(
        @SerializedName("IV_WERKS")
        val werks: String, //Предп
        @SerializedName("IV_MATNR")
        val materialNumber: String, //Номер товара
        @SerializedName("IV_MATNR_COMP")
        val materialNumberComp: String, //Номер компонета набора
        @SerializedName("IV_MARK_NUM")
        val stampCode: String, //Код акцизной марки
        @SerializedName("IV_BOX_NUM")
        val boxNumber: String, //Номер коробки
        @SerializedName("IV_ZPROD")
        val manufacturerCode: String, //ЕГАИС Код организации
        @SerializedName("IV_BOTT_MARK")
        val bottlingDate: String, //УТЗ ТСД: Дата розлива
        @SerializedName("IV_MODE")
        val mode: String, //Индикатор из одной позиции
        @SerializedName("IV_CODEBP")
        val codeEBP: String, //Код БП УТЗ
        @SerializedName("IV_FACT_QNT")
        val factCount: String //Фактическое количество (для партионных)
)