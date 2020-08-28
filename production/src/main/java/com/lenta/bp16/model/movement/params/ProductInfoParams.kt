package com.lenta.bp16.model.movement.params

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.Ean
import com.lenta.bp16.request.pojo.Product

data class ProductInfoParams(
        @SerializedName("IT_MATNR_LIST")
        val matnr: List<Product>,
        @SerializedName("IT_EAN_LIST")
        val ean: List<Ean>
)