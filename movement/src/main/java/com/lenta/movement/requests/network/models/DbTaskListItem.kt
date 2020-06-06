package com.lenta.movement.requests.network.models

import com.google.gson.annotations.SerializedName
import com.lenta.movement.models.MovementType
import com.lenta.movement.models.TaskType

data class DbTaskListItem(
    @SerializedName("TASK_NUM")
    val taskNumber: String,
    @SerializedName("DESCR")
    val description: String,
    @SerializedName("TASK_TYPE")
    val taskType: TaskType,
    @SerializedName("TYPE_MVM")
    val movementType: MovementType,
    @SerializedName("QNT_POS")
    val quantityPosition: String,
    @SerializedName("LGORT_SRC")
    val lgortSrc: String,
    @SerializedName("LGORT_TGT")
    val lgortTarget: String,
    @SerializedName("WERKS_DSTNTN")
    val werksDstntnt: String,
    @SerializedName("BLOCK_TYPE")
    val blockType: String,
    @SerializedName("LOCK_USER")
    val lockUser: String,
    @SerializedName("LOCK_IP")
    val lockIp: String,
    @SerializedName("NOT_FINISH")
    val notFinish: String,
    @SerializedName("IS_CONS")
    val isCons: String,
    @SerializedName("TASK_CNTRL")
    val taskCntrl: String,
    @SerializedName("TASK_COMMENT")
    val taskComment: String,
    @SerializedName("DATE_SHIP")
    val dateShip: String,
    @SerializedName("CUR_STAT")
    val currentStatusCode: String,
    @SerializedName("CUR_STAT_TEXT")
    val currentStatusText: String,
    @SerializedName("NEXT_STAT_TEXT")
    val nextStatusText: String,
    @SerializedName("ERROR_TEXT")
    val errorText: String
)