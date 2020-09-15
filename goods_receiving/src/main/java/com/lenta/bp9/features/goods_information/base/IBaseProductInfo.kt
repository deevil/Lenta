package com.lenta.bp9.features.goods_information.base

interface IBaseProductInfo : IBaseVariables {
    val productMaterialNumber
        get() = productInfo.value?.materialNumber.orEmpty()

    val productPurchaseOrderUnitsName
        get() = productInfo.value?.purchaseOrderUnits?.name.orEmpty()
}