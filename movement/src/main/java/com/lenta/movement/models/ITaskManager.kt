package com.lenta.movement.models

interface ITaskManager {

    fun getTask(): Task

    fun setTask(task: Task)

    fun setOnTaskChanges(block: (Task) -> Unit)

    fun getAvailableReceivers(): List<String>

    fun getAvailablePikingStorageList(): List<String>

    fun getAvailableShipmentStorageList(): List<String>

    fun getTaskAnnotation(): String

    fun clear()
}