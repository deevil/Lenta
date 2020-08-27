package com.lenta.bp16.model.movement.params

import com.google.gson.annotations.SerializedName

data class WarehouseParams(
        /**ТК*/
        @SerializedName("IV_PLANT")
        val werks: String
)