package com.lenta.bp10.models.task

import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.shared.models.core.ProductInfo

class TaskSaveModel(val taskDescription: TaskDescription, val taskRepository: ITaskRepository) {

    fun getPerNo(): String {
        return taskDescription.perNo
    }

    fun getPrinter(): String {
        return taskDescription.printer
    }

    fun getTaskName(): String {
        return taskDescription.taskName
    }

    fun getTaskType(): TaskType {
        return taskDescription.taskType
    }

    fun getTkNumber(): String {
        return taskDescription.tkNumber
    }

    fun getStorloc(): String {
        return taskDescription.stock
    }

    fun getIpAdress(): String {
        return taskDescription.ipAddress
    }

    fun getMoveTypes(): List<WriteOffReason> {
        return taskDescription.moveTypes
    }

    fun getGisControls(): List<String> {
        return taskDescription.gisControls
    }

    fun getMaterialTypes(): List<String> {
        return taskDescription.materialTypes
    }

    fun getMaterials(): List<ProductInfo> {
        return taskRepository.getProducts().getProducts()
    }

    fun getWriteOffReasons(): List<TaskWriteOffReason> {
        return taskRepository.getWriteOffReasons().getWriteOffReasons()
    }

    fun getExciseStamps(): List<TaskExciseStamp> {
        return taskRepository.getExciseStamps().getExciseStamps()
    }
}