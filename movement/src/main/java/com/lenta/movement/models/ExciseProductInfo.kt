package com.lenta.movement.models

import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom

class ExciseProductInfo(
    materialNumber: String,
    description: String,
    uom: Uom,
    type: ProductType,
    isSet: Boolean,
    sectionId: String,
    matrixType: MatrixType,
    materialType: String,
    val quantityInvestments: Int,
    val isRus: Boolean
): ProductInfo(
    materialNumber, description, uom, type, isSet, sectionId, matrixType, materialType
)