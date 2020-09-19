package com.lenta.bp9.features.goods_information.base

import com.lenta.bp9.features.base.IBaseTaskManager
import com.lenta.bp9.model.task.TaskType

interface IBaseCountAcceptOfProductByTaskType : IBaseVariables,
        IBaseTaskManager,
        IBaseProductInfo
{
    val countAcceptOfProductByTaskType: Double
        get()
        {
            return productInfo.value
                    ?.let { product ->
                        if (taskType == TaskType.RecalculationCargoUnit) {
                            getCountAcceptOfProductPGE(product)
                        } else {
                            getCountAcceptOfProduct(product)
                        }
                    }
                    ?: 0.0
        }
}