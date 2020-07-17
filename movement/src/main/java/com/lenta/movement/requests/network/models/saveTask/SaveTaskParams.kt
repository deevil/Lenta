package com.lenta.movement.requests.network.models.saveTask

import com.google.gson.annotations.SerializedName
import com.lenta.movement.models.MovementType
import com.lenta.movement.models.TaskType

data class SaveTaskParams(
        @SerializedName("IV_IP_PDA")
        val deviceIp: String,
        @SerializedName("IV_PERNR")
        val userNumber: String,
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_DESCR")
        val taskName: String,
        @SerializedName("IV_TYPE")
        val taskType: TaskType,
        @SerializedName("IV_TYPE_MVM")
        val movementType: MovementType,
        @SerializedName("IV_LGORT_SRC")
        val lgortSource: String,
        @SerializedName("IV_LGORT_TGT")
        val lgortTarget: String,
        @SerializedName("IV_DATE_SHIP")
        val shipmentDate: String,
        @SerializedName("IV_NOT_FINISH")
        val isNotFinish: String,
        @SerializedName("IV_WERKS_DSTNTN")
        val destination: String,
        @SerializedName("IT_TASK_POS")
        val materials: List<SaveTaskParamsTaskMaterial>,
        @SerializedName("IT_TASK_BASKET")
        val baskets: List<SaveTaskParamsTaskBasket>
)