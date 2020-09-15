package com.lenta.bp9.features.goods_information.base

import com.lenta.bp9.features.base.IBaseTaskManager
import com.lenta.bp9.model.task.TaskType

interface IBaseCountRefusalOfProductByTaskType : IBaseTaskManager, IBaseProductInfo {
    val countRefusalOfProduct: Double
        get() {
            val productsDiscrepancies = taskRepository?.getProductsDiscrepancies()

            return productInfo.value
                    ?.let { product ->
                        if (taskType == TaskType.RecalculationCargoUnit) {
                            productsDiscrepancies?.getCountRefusalOfProductPGE(product)
                        } else {
                            productsDiscrepancies?.getCountRefusalOfProduct(product)
                        }
                    }
                    ?: 0.0
        }
}