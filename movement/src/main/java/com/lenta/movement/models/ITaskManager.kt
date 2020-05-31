package com.lenta.movement.models

interface ITaskManager {

    fun getTaskSettings(
        taskType: TaskType = getTask().taskType,
        movementType: MovementType = getTask().movementType
    ): TaskSettings

    fun getTask(): Task

    fun getTaskOrNull(): Task?

    fun setTask(task: Task)

    fun getAvailableReceivers(): List<String>

    fun getAvailablePikingStorageList(taskType: TaskType, movementType: MovementType): List<String>

    fun isAllowProduct(product: ProductInfo): Boolean

    fun isDisallowProduct(product: ProductInfo): Boolean

    fun clear()
}