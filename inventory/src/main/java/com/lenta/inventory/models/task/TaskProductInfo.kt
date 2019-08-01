package com.lenta.inventory.models.task

import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.toStringFormatted

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

    fun getDisplayName() : String {
        return "${getMaterialLastSix()} ${description}"
    }

    fun getFormattedCount() : String {
        return "${factCount.toStringFormatted()} ${uom.name}"
    }

    companion object {
        fun from(productInfo: ProductInfo, placeCode: String = "00", factCount: Double = 0.0) : TaskProductInfo {
            return TaskProductInfo(materialNumber = productInfo.materialNumber,
                    description = productInfo.description,
                    uom = productInfo.uom,
                    type = productInfo.type,
                    isSet = productInfo.isSet,
                    sectionId = productInfo.sectionId,
                    matrixType = productInfo.matrixType,
                    materialType = productInfo.materialType,
                    placeCode = placeCode,
                    factCount = factCount,
                    isPositionCalc = false,
                    isExcOld = false
                    )
        }
    }

}