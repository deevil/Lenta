package com.lenta.bp9.models.task

class TaskList(
        val tasks: List<TaskInfo>,
        val taskCount: Int,
        val error: String,
        val retcode: String) {

}