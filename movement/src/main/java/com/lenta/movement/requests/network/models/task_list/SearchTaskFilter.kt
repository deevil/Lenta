package com.lenta.movement.requests.network.models.task_list

import com.google.gson.annotations.SerializedName

data class SearchTaskFilter(
        @SerializedName("TASK_TYPE")
        val taskType: String = "",
        @SerializedName("MATNR")
        val matNr: String = "",
        @SerializedName("ABTNR")
        val sectionId: String = "",
        @SerializedName("MATKL")
        val group: String = "",
        @SerializedName("DATE_PUBLIC")
        val dateOfPublic: String = ""
) {
    fun isEmpty(): Boolean {
        return taskType.isBlank() && matNr.isBlank() && sectionId.isBlank() && group.isBlank() && dateOfPublic.isBlank()
    }
}