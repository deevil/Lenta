package com.lenta.movement.models

import com.google.gson.annotations.SerializedName

enum class TaskType(val shortName: String) {
    @SerializedName("ТРЗ")
    TransferWithOrder("ТРЗ"),
    @SerializedName("ТРБ")
    TransferWithoutOrder("ТРБ")
}