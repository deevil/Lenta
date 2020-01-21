package com.lenta.bp9.model.task

import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode

class TaskList(
        val tasks: List<TaskInfo>,
        val taskCount: Int,
        val taskListLoadingMode: TaskListLoadingMode) {

}