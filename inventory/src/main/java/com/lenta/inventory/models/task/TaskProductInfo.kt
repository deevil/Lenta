package com.lenta.inventory.models.task

import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom

class TaskProductInfo(materialNumber: String,
                      description: String,
                      uom: Uom,
                      type: ProductType,
                      isSet: Boolean,
                      sectionId: String,
                      matrixType: MatrixType,
                      materialType: String,
                      val placeCode: String,
                      var factCount: Double = 0.0,
                      var isPositionCalc: Boolean,
                      val isExcOld: Boolean) : ProductInfo(materialNumber, description, uom, type, isSet, sectionId, matrixType, materialType) {
}