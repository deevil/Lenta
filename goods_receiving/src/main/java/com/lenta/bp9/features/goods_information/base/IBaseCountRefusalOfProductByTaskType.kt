package com.lenta.bp9.features.goods_information.base

import com.lenta.bp9.features.base.IBaseTaskManager
import com.lenta.bp9.model.task.TaskType

interface IBaseCountRefusalOfProductByTaskType : IBaseTaskManager, IBaseProductInfo {
    val countRefusalOfProductByTaskType: Double
        get() {
            return productInfo.value
                    ?.let { product ->
                        if (taskType == TaskType.RecalculationCargoUnit) {
                            getCountRefusalOfProductPGE(product)
                        } else {
                            getCountRefusalOfProduct(product)
                        }
                    }
                    ?: 0.0
        }
}