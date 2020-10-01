package com.lenta.bp10.models.task

import com.lenta.bp10.models.memory.isContainsStamp
import com.lenta.bp10.models.repositories.IProcessProductService
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.repositories.getTotalCountForProduct
import com.lenta.shared.models.core.ProductInfo

class ProcessMarkedGoodProductService(
        val taskDescription: TaskDescription,
        val taskRepository: ITaskRepository,
        val productInfo: ProductInfo
) : IProcessProductService {

    override fun getTotalCount(): Double {
        return taskRepository.getTotalCountForProduct(productInfo)
    }

    override fun apply(): WriteOffTask {
        return WriteOffTask(taskDescription, taskRepository)
    }

    override fun discard(): WriteOffTask {
        return WriteOffTask(taskDescription, taskRepository)
    }

    fun add(writeOffReason: WriteOffReason, count: Double, stamp: TaskExciseStamp? = null): ProcessMarkedGoodProductService {
        // Добавить товар если его нету в таске товаров,
        // в репозитории найти причину списания для данного товара,
        // если есть, то увеличить count иначе создать новый, добавить марку
        if (stamp != null && taskRepository.getExciseStamps().isContainsStamp(stamp.code)) {
            return ProcessMarkedGoodProductService(taskDescription, taskRepository, productInfo)
        }

        var taskWriteOfReason = TaskWriteOffReason(writeOffReason, productInfo.materialNumber, count)
        if (taskRepository.getProducts().findProduct(productInfo) == null) {
            taskRepository.getProducts().addProduct(productInfo)
            taskRepository.getWriteOffReasons().addWriteOffReason(taskWriteOfReason)
        } else {
            val arrTaskWriteOffReason = taskRepository.getWriteOffReasons().findWriteOffReasonsOfProduct(productInfo)
            var lastIndex = -1

            arrTaskWriteOffReason.forEachIndexed { index, reason ->
                if (writeOffReason == reason.writeOffReason) {
                    taskRepository.getWriteOffReasons().deleteWriteOffReason(taskWriteOfReason)
                    val newCount = arrTaskWriteOffReason[index].count + count
                    taskWriteOfReason = TaskWriteOffReason(writeOffReason, productInfo.materialNumber, newCount)
                    taskRepository.getWriteOffReasons().addWriteOffReason(taskWriteOfReason)

                    lastIndex = index
                }
            }

            if (lastIndex == -1) {
                taskRepository.getWriteOffReasons().addWriteOffReason(taskWriteOfReason)
            }
        }

        addStamp(writeOffReason, stamp)
        return ProcessMarkedGoodProductService(taskDescription, taskRepository, productInfo)
    }

    fun addStamp(reason: WriteOffReason, stamp: TaskExciseStamp?): Boolean {

        if (stamp == null || taskRepository.getExciseStamps().isContainsStamp(stamp.code)) {
            return false
        }

        taskRepository.getExciseStamps().addExciseStamp(stamp.copy(reason.code))

        return true
    }

}