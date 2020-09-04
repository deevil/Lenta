package com.lenta.bp9.features.goods_information.baseGoods

interface IBaseCountRefusalOfProduct : IBaseVariables, IBaseProductInfo {
    val countRefusalOfProduct: Double
        get() {
            return productInfo.value
                    ?.let { product ->
                        taskManager
                                .getReceivingTask()
                                ?.taskRepository
                                ?.getProductsDiscrepancies()
                                ?.getCountRefusalOfProduct(product)
                    }
                    ?: 0.0
        }
}