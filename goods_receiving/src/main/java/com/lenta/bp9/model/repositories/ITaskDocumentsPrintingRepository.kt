package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskDocumentsPrinting

interface ITaskDocumentsPrintingRepository {
    fun getDocumentsPrinting(): List<TaskDocumentsPrinting>
    fun updateDocumentsPrinting(newDocs: List<TaskDocumentsPrinting>)
    fun clear()
}