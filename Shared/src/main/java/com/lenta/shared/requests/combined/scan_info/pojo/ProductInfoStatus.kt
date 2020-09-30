package com.lenta.shared.requests.combined.scan_info.pojo

import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.ObjectRawStatus


class ProductInfoStatus : ObjectRawStatus<ProductServerInfo>()


data class ProductInfoNetRequestParams(
        @SerializedName("IV_EAN")
        val ean: String,
        @SerializedName("IV_WERKS")
        val tk: String,
        @SerializedName("IV_MATNR")
        val matNr: String
)




data class ProductServerInfo(
        @SerializedName("ES_EAN")
        val ean: Ean?,
        @SerializedName("ES_MATERIAL")
        val material: Material?,
        @SerializedName("ET_SET")
        val set: List<Set>?,
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String,
        @SerializedName("EV_RETCODE")
        val retCode: Int
)

data class Material(
        @SerializedName("ABTNR")
        val abtnr: String?,
        @SerializedName("BUOM")
        val buom: String?,
        @SerializedName("EKGRP")
        val ekgrp: String?,
        @SerializedName("IS_ALCO")
        val isAlco: String?,
        @SerializedName("IS_EXC")
        val isExcise: String?,
        @SerializedName("IS_RETURN")
        val isReturn: String?,
        @SerializedName("MATERIAL")
        val material: String?,
        @SerializedName("MATKL")
        val matkl: String?,
        @SerializedName("MATR_TYPE")
        val matrixType: String?,
        @SerializedName("MATYPE")
        val materialType: String?,
        @SerializedName("NAME")
        val name: String?,
        @SerializedName("IS_MARK")
        var isMark: String?,
        @SerializedName("ZMARKTYPE")
        var markType: String?
)

data class Ean(
        @SerializedName("EAN")
        val ean: String?,
        @SerializedName("MATERIAL")
        val materialNumber: String?,
        @SerializedName("UMREN")
        val umren: Int?,
        @SerializedName("UMREZ")
        val umrez: Int?,
        @SerializedName("UOM")
        val uom: String?
)

fun Ean.toEan(): EanInfo {
        return EanInfo(
                ean = ean.orEmpty(),
                materialNumber = materialNumber.orEmpty(),
                umrez = umrez ?: 0,
                umren = umren ?: 0,
                uom = uom.orEmpty()
        )
}


data class Set(
        @SerializedName("MATNR")
        val matNr: String?,
        @SerializedName("MATNR_OSN")
        val matNrOsn: String?,
        @SerializedName("MEINS")
        val meins: String?,
        @SerializedName("MENGE")
        val menge: String?
)