package com.lenta.bp10.models.task

import com.lenta.bp10.models.repositories.IProcessProductService
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.repositories.getTotalCountForProduct
import com.lenta.shared.models.core.ProductInfo

class ProcessGeneralProductService(
        val taskDescription: TaskDescription,
        val taskRepository: ITaskRepository,
        val productInfo: ProductInfo
) : IProcessProductService {

    override fun getTotalCount(): Double {
        // (Артем И., 09.04.2019) по данному продукту ИТОГО причин списания
        return taskRepository.getTotalCountForProduct(productInfo)
    }

    override fun apply(): WriteOffTask {
        return WriteOffTask(taskDescription, taskRepository)
    }

    override fun discard(): WriteOffTask {
        return WriteOffTask(taskDescription, taskRepository)
    }

    fun add(reason: WriteOffReason, count: Double): ProcessGeneralProductService {
        // (Артем И., 09.04.2019) добавить товар если его нету в таске товаров, в репозитории найти причину списания для данного товара, если есть, то увеличить count иначе создать новый
        var taskWriteOfReason = TaskWriteOffReason(reason, productInfo.materialNumber, count)
        taskRepository.getProducts().addProduct(productInfo)
        if (taskRepository.getProducts().findProduct(productInfo) == null) {
            taskRepository.getWriteOffReasons().addWriteOffReason(taskWriteOfReason)
        } else {
            val arrTaskWriteOffReason = taskRepository.getWriteOffReasons().findWriteOffReasonsOfProduct(productInfo)
            var index = -1
            for (i in arrTaskWriteOffReason.indices) {
                if (reason == arrTaskWriteOffReason[i].writeOffReason) {
                    taskRepository.getWriteOffReasons().deleteWriteOffReason(taskWriteOfReason)
                    val newCount = arrTaskWriteOffReason[i].count + count
                    if (newCount > 0.0) {
                        taskWriteOfReason = TaskWriteOffReason(reason, productInfo.materialNumber, newCount)
                        taskRepository.getWriteOffReasons().addWriteOffReason(taskWriteOfReason)
                    }
                    index = i
                }
            }

            if (index == -1) {
                taskRepository.getWriteOffReasons().addWriteOffReason(taskWriteOfReason)
            }

        }

        if (getTotalCount() <= 0.0) {
            taskRepository.getProducts().deleteProduct(productInfo)
        }

        return ProcessGeneralProductService(taskDescription, taskRepository, productInfo)
    }

}