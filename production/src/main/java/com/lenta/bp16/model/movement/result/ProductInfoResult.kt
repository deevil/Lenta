package com.lenta.bp16.model.movement.result

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.ProducerInfo
import com.lenta.bp16.request.pojo.ProductInfo
import com.lenta.bp16.request.pojo.SetInfo

data class ProductInfoResult(
        @SerializedName("ET_MATNR_LIST")
        val product: List<ProductInfo>?,
        @SerializedName("ET_SET_LIST")
        val set: List<SetInfo>?,
        @SerializedName("ET_PROD_TEXT")
        val producers: List<ProducerInfo>?
)