package com.lenta.bp9.model.task

import com.lenta.bp9.model.ReceivingProductInfo
import com.lenta.bp9.model.repositories.ITaskRepository
import com.lenta.bp9.model.task.revise.*

class ReceivingTask(val taskHeader: TaskInfo,
                    val taskDescription: TaskDescription,
                    val taskRepository: ITaskRepository) {

    fun getProcessedProducts(): List<ReceivingProductInfo> {
        return taskRepository.getProducts().getProducts()
    }
}



