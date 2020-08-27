package com.lenta.bp16.model.movement.result

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.movement.ui.Warehouse

data class WarehouseResult (
        /**Список складов*/
        @SerializedName("ET_STORLOCS")
        val warehouseList: List<Warehouse>?
)