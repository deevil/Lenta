package com.lenta.bp9.model

import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom

class ReceivingProductInfo(materialNumber: String,
                           description: String,
                           uom: Uom,
                           type: ProductType,
                           isSet: Boolean,
                           sectionId: String,
                           matrixType: MatrixType,
                           materialType: String) : ProductInfo(materialNumber, description, uom, type, isSet, sectionId, matrixType, materialType) {

    fun copy(materialNumber: String = this.materialNumber,
             description: String = this.description,
             uom: Uom = this.uom,
             type:ProductType = this.type,
             isSet: Boolean = this.isSet,
             sectionId: String = this.sectionId,
             matrixType: MatrixType = this.matrixType,
             materialType: String = this.materialType) : ReceivingProductInfo {
        return ReceivingProductInfo(
                materialNumber = materialNumber,
                description = description,
                uom = uom,
                type = type,
                isSet = isSet,
                sectionId = sectionId,
                matrixType = matrixType,
                materialType = materialType
        )
    }
}