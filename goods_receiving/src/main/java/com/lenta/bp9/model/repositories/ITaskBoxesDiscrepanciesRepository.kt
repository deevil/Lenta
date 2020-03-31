package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskBoxDiscrepancies
import com.lenta.bp9.model.task.TaskBoxInfo

interface ITaskBoxesDiscrepanciesRepository {
    fun getBoxesDiscrepancies(): List<TaskBoxDiscrepancies>
    fun findDiscrepanciesOfBox(findBox: TaskBoxInfo): List<TaskBoxDiscrepancies>
    fun addBoxDiscrepancies(discrepancies: TaskBoxDiscrepancies): Boolean
    fun updateBoxesDiscrepancy(newBoxesDiscrepancies: List<TaskBoxDiscrepancies>)
    fun changeBoxDiscrepancy(discrepancy: TaskBoxDiscrepancies): Boolean
    fun deleteBoxDiscrepancies(discrepancies: TaskBoxDiscrepancies): Boolean
    fun deleteBoxesDiscrepanciesForBox(delBox: TaskBoxInfo): Boolean
    fun clear()
}