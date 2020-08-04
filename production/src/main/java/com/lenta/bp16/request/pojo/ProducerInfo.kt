package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class ProducerInfo (
        /**Код производителя*/
        @SerializedName("PROD_CODE")
        val prodCode: String?,
        /**Название производителя*/
        @SerializedName("PROD_NAME")
        val prodName: String?
)