package com.lenta.bp10.models.task

class TaskDescription(val taskType: TaskType, val taskName: String, private val storloc: String, val moveTypes: List<String>, val gisControls: List<String>, val materialTypes: List<String>) {

    val selectedStorloc: String
        get() = this.storloc
}