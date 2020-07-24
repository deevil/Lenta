package com.lenta.movement.models

interface ITaskManager{

    suspend fun getTaskSettings(
        taskType: TaskType = getTask().taskType,
        movementType: MovementType = getTask().movementType
    ): TaskSettings

    fun getTask(): Task

    fun getTaskOrNull(): Task?

    fun setTask(task: Task)

    suspend fun getAvailableReceivers(): List<String>

    suspend fun getAvailablePikingStorageList(taskType: TaskType, movementType: MovementType): List<String>

    suspend fun isAllowProduct(product: ProductInfo): Boolean

    suspend fun isDisallowProduct(product: ProductInfo): Boolean

    fun clear()

    suspend fun getPrinterName() : String

    suspend fun getGoodName(goodNumber: String?) : String

    suspend fun getMovementType(movementType: MovementType) : String
    suspend fun getMovementTypeShort(movementType: MovementType) : String
}