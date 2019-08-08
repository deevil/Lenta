package com.lenta.bp9.models.task

import com.google.gson.annotations.SerializedName

data class DirectSupplierTaskListRestInfo(
        @SerializedName("ET_TASK_LIST")
        val tasks: List<DirectSupplierTaskRestInfo>,
        @SerializedName("EV_ERROR_TEXT")
        val error: String,
        @SerializedName("EV_RETCODE")
        val retcode: String
)

data class DirectSupplierTaskRestInfo (
        @SerializedName("TASK_NUM")
        val taskNumber: String
)