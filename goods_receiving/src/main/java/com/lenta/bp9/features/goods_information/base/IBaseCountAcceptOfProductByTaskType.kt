package com.lenta.bp9.features.goods_information.base

import com.lenta.bp9.features.base.IBaseTaskManager
import com.lenta.bp9.model.task.TaskType

interface IBaseCountAcceptOfProductByTaskType : IBaseTaskManager, IBaseProductInfo {
    val countAcceptOfProduct: Double
        get()
        {
            val productsDiscrepancies = taskRepository?.getProductsDiscrepancies()

            return productInfo.value
                    ?.let { product ->
                        if (taskType == TaskType.RecalculationCargoUnit) {
                            productsDiscrepancies?.getCountAcceptOfProductPGE(product)
                        } else {
                            productsDiscrepancies?.getCountAcceptOfProduct(product)
                        }
                    }
                    ?: 0.0
        }
}