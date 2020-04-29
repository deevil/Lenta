package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskBoxDiscrepancies
import com.lenta.bp9.model.task.TaskBoxInfo
import com.lenta.bp9.model.task.TaskProductInfo

interface ITaskBoxesDiscrepanciesRepository {
    fun getBoxesDiscrepancies(): List<TaskBoxDiscrepancies>
    fun findBoxesDiscrepanciesOfBox(box: TaskBoxInfo): List<TaskBoxDiscrepancies>
    fun findBoxesDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskBoxDiscrepancies>
    fun addBoxDiscrepancies(discrepancies: TaskBoxDiscrepancies): Boolean
    fun updateBoxesDiscrepancy(newBoxesDiscrepancies: List<TaskBoxDiscrepancies>)
    fun changeBoxDiscrepancy(discrepancy: TaskBoxDiscrepancies): Boolean
    fun deleteBoxDiscrepancies(discrepancies: TaskBoxDiscrepancies): Boolean
    fun deleteBoxDiscrepancies(materialNumber: String, boxNumber: String, typeDiscrepancies: String): Boolean
    fun deleteBoxesDiscrepanciesForBox(delBox: TaskBoxInfo): Boolean
    fun clear()
}