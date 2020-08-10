package com.lenta.movement.requests.network.models.obtainingDataExciseGoodsNetRequest

import com.google.gson.annotations.SerializedName
import com.lenta.shared.models.core.Manufacturer

data class ExciseGoodsRestInfo(
        @SerializedName("EV_STAT")
        val status: InfoStatus?, //Поле CHAR3
        @SerializedName("EV_STAT_TEXT")
        val statusTxt: String?, //Текст статуса для отображения в МП
        @SerializedName("ET_MARKS")
        val stampsBox: List<StampsBox>?, //Таблица марок в коробке
        @SerializedName("EV_MATNR_COMP")
        val materialNumber: String?, //Номер товара
        @SerializedName("ET_PROD_TEXT")
        val manufacturers: List<Manufacturer>?, //Таблица ЕГАИС производителей
        @SerializedName("EV_ZCHARG")
        val batchNumber: String?, //Номер партии
        @SerializedName("EV_DATEOFPOUR")
        val dateManufacture: String?, //Дата производства
        @SerializedName("EV_ERROR_TEXT")
        val errorTxt: String?, //Текст ошибки
        @SerializedName("EV_RETCODE")
        val retCode: String? //Код возврата для ABAP-операторов
) {
    enum class InfoStatus {
        @SerializedName("01")
        StampFound,

        @SerializedName("02")
        StampOverload,

        @SerializedName("03")
        StampOfOtherProduct,

        @SerializedName("04")
        StampNotFound
    }
}