package com.lenta.bp16.model.movement.ui

import com.google.gson.annotations.SerializedName

data class Warehouse (
        /**Склад*/
        @SerializedName("STORLOC")
        val warehouseName: String,
        /**Индикатор блокировки*/
        @SerializedName("LOCKED")
        val warehouseLock: String
)