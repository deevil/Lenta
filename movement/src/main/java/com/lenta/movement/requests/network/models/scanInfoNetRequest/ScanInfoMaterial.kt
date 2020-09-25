package com.lenta.movement.requests.network.models.scanInfoNetRequest

import com.google.gson.annotations.SerializedName

data class ScanInfoMaterial(
        @SerializedName("ABTNR")
        val abtnr: String?,
        @SerializedName("BUOM")
        val buom: String?,
        @SerializedName("EKGRP")
        val ekgrp: String?,
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
        @SerializedName("QNTINCL")
        val quantityInvestments: String?,
        @SerializedName("VOLUM")
        val volume: String?,
        @SerializedName("IS_RUS")
        val isRus: String?,
        @SerializedName("IS_ALCO")
        val isAlco: String?,
        @SerializedName("IS_EXC")
        val isExcise: String?,
        @SerializedName("IS_RETURN")
        val isReturn: String?,
        @SerializedName("IS_VET")
        val isVet: String?,
        @SerializedName("IS_FOOD")
        val isFood: String?,
        @SerializedName("ZMARKTYPE")
        val markType: String?
)