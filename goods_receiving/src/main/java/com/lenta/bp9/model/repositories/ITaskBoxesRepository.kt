package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskBoxInfo
import com.lenta.bp9.model.task.TaskExciseStampInfo
import com.lenta.bp9.model.task.TaskProductInfo

interface ITaskBoxesRepository {
    fun getBoxes(): List<TaskBoxInfo>
    fun findBox(box: TaskBoxInfo): TaskBoxInfo?
    fun findBoxesOfProduct(productInfo: TaskProductInfo): List<TaskBoxInfo>?
    fun addBox(box: TaskBoxInfo): Boolean
    fun updateBoxes(newBoxes: List<TaskBoxInfo>)
    fun changeBoxes(box: TaskBoxInfo): Boolean
    fun deleteBox(box: TaskBoxInfo): Boolean
    fun deleteBoxes(delBoxes: List<TaskBoxInfo>): Boolean
    fun deleteBoxesForProduct(product: TaskProductInfo): Boolean
    fun clear()
}