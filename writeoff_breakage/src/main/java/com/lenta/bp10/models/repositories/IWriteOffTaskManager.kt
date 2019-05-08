package com.lenta.bp10.models.repositories

import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.models.task.WriteOffTask

interface IWriteOffTaskManager {

    fun getWriteOffTask() : WriteOffTask?

    fun newWriteOffTask(taskDescription: TaskDescription)

    fun clearTask()

    fun setTask(apply: WriteOffTask?)


}