package com.lenta.movement.models

import com.google.gson.annotations.SerializedName

enum class TaskType {
    @SerializedName("ТРЗ")
    TransferWithOrder,
    @SerializedName("ТРБ")
    TransferWithoutOrder
}