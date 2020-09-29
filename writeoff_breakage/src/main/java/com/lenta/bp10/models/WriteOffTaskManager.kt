package com.lenta.bp10.models


import com.lenta.bp10.models.memory.MemoryTaskRepository
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.models.task.WriteOffTask

class WriteOffTaskManager(
        private var currentWriteOffTask: WriteOffTask? = null
) : IWriteOffTaskManager {

    override fun getWriteOffTask(): WriteOffTask? {
        return currentWriteOffTask
    }

    override fun newWriteOffTask(taskDescription: TaskDescription) {
        currentWriteOffTask = WriteOffTask(taskDescription, taskRepository = MemoryTaskRepository())
    }

    override fun clearTask() {
        currentWriteOffTask = null

    }

    override fun setTask(writeOffTask: WriteOffTask?) {
        currentWriteOffTask = writeOffTask
    }


}