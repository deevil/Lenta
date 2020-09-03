package com.lenta.bp9.features.goods_information.baseGoods

interface IBaseCountAcceptOfProduct : IBaseVariables, IBaseProductInfo {
    val countAcceptOfProduct: Double
        get()
        {
            return productInfo.value
                    ?.let { product ->
                        taskManager
                                .getReceivingTask()
                                ?.taskRepository
                                ?.getProductsDiscrepancies()
                                ?.getCountAcceptOfProduct(product)
                    }
                    ?: 0.0
        }
}