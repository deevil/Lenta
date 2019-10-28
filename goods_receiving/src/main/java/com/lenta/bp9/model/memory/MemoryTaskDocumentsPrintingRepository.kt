package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskDocumentsPrintingRepository
import com.lenta.bp9.model.task.TaskDocumentsPrinting

class MemoryTaskDocumentsPrintingRepository : ITaskDocumentsPrintingRepository {

    private val documentsPrinting: ArrayList<TaskDocumentsPrinting> = ArrayList()

    override fun getDocumentsPrinting(): List<TaskDocumentsPrinting> {
        return documentsPrinting
    }

    override fun updateDocumentsPrinting(newDocs: List<TaskDocumentsPrinting>) {
        clear()
        newDocs.map {
            documentsPrinting.add(it)
        }
    }

    override fun clear() {
        documentsPrinting.clear()
    }
}