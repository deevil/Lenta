package com.lenta.inventory.models.task

import com.lenta.inventory.models.repositories.ITaskRepository
import com.lenta.shared.models.core.ProductInfo

class ProcessExciseAlcoProductService(val taskDescription: TaskDescription,
                                      val taskRepository: ITaskRepository,
                                      val productInfo: ProductInfo) : IProcessProductService {
    override fun getTotalCount(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun apply(): InventoryTask {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun discard(): InventoryTask {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}