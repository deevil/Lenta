package com.lenta.bp9.features.base

import com.lenta.bp9.model.repositories.ITaskProductRepository
import com.lenta.bp9.model.repositories.ITaskProductsDiscrepanciesRepository
import com.lenta.bp9.model.repositories.ITaskRepository
import com.lenta.bp9.model.task.*

interface IBaseTaskManager {
    val taskManager: IReceivingTaskManager

    /**---------BEGIN taskManager---------*/
    val receivingTask: ReceivingTask?
        get() = taskManager.getReceivingTask()

    val taskRepository: ITaskRepository?
        get() = taskManager.getReceivingTask()?.taskRepository

    val taskDescription: TaskDescription?
        get() = taskManager.getReceivingTask()?.taskDescription
    /**---------END taskManager---------*/

    /**---------BEGIN taskDescription---------*/
    /**---------END taskDescription---------*/


    /**---------BEGIN taskHeader---------*/
    val taskHeader: TaskInfo?
        get() = taskManager.getReceivingTask()?.taskHeader

    val taskType: TaskType
        get() = taskHeader
                ?.taskType
                ?: TaskType.None

    val taskNumber: String
        get() = taskHeader?.taskNumber.orEmpty()
    /**---------END taskHeader---------*/

    /**---------BEGIN receivingTask---------*/
    val processedProductsDiscrepancies: List<TaskProductDiscrepancies>?
        get() = receivingTask?.getProcessedProductsDiscrepancies()

    val processedBatchesDiscrepancies: List<TaskBatchesDiscrepancies>?
        get() = receivingTask?.getProcessedBatchesDiscrepancies()

    val processedBoxesDiscrepancies: List<TaskBoxDiscrepancies>?
        get() = receivingTask?.getProcessedBoxesDiscrepancies()

    val processedExciseStampsDiscrepancies: List<TaskExciseStampDiscrepancies>?
        get() = receivingTask?.getProcessedExciseStampsDiscrepancies()

    val processedExciseStampsBad: List<TaskExciseStampBad>?
        get() = receivingTask?.getProcessedExciseStampsBad()

    val processedMercuryDiscrepancies: List<TaskMercuryDiscrepancies>?
        get() = receivingTask?.getProcessedMercuryDiscrepancies()

    val processedBlocksDiscrepancies: List<TaskBlockDiscrepancies>?
        get() = receivingTask?.getProcessedBlocksDiscrepancies()

    val processedZBatchesDiscrepancies: List<TaskZBatchesDiscrepancies>?
        get() = receivingTask?.getProcessedZBatchesDiscrepancies()
    /**---------END receivingTask---------*/


    /**---------BEGIN taskRepository---------*/
    fun getProcessingUnitsOfProduct(materialNumber: String): List<TaskProductInfo>? {
        return taskRepository
                ?.getProducts()
                ?.getProcessingUnitsOfProduct(materialNumber)
    }

    fun getCountProductNotProcessedOfProductPGEOfProcessingUnits(product: TaskProductInfo, countOrderQuantity: Double): Double {
        return taskRepository
                ?.getProductsDiscrepancies()
                ?.getCountProductNotProcessedOfProductPGEOfProcessingUnits(product, countOrderQuantity)
                ?: 0.0
    }

    fun getCountProductNotProcessedOfProductPGE(product: TaskProductInfo): Double {
        return taskRepository
                ?.getProductsDiscrepancies()
                ?.getCountProductNotProcessedOfProductPGE(product)
                ?: 0.0
    }

    fun getCountAcceptOfProduct(product: TaskProductInfo): Double {
        return taskRepository
                ?.getProductsDiscrepancies()
                ?.getCountAcceptOfProduct(product)
                ?: 0.0
    }

    fun getCountAcceptOfProductPGE(product: TaskProductInfo): Double {
        return taskRepository
                ?.getProductsDiscrepancies()
                ?.getCountAcceptOfProductPGE(product)
                ?: 0.0
    }

    fun getCountRefusalOfProductPGE(product: TaskProductInfo): Double {
        return taskRepository
                ?.getProductsDiscrepancies()
                ?.getCountRefusalOfProductPGE(product)
                ?: 0.0
    }

    fun getCountRefusalOfProduct(product: TaskProductInfo): Double {
        return taskRepository
                ?.getProductsDiscrepancies()
                ?.getCountRefusalOfProduct(product)
                ?: 0.0
    }
    /**---------END taskRepository---------*/

}
