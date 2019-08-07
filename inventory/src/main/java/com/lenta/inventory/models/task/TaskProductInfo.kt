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
                      val factCount: Double = 0.0,
                      val isPositionCalc: Boolean,
                      val isDel: Boolean = false,
                      val isExcOld: Boolean) : ProductInfo(materialNumber, description, uom, type, isSet, sectionId, matrixType, materialType) {

    fun getDisplayName() : String {
        return "${getMaterialLastSix()} ${description}"
    }

    fun getFormattedCount() : String {
        return "${factCount.toStringFormatted()} ${uom.name}"
    }

    companion object {

        fun changeCopy(productInfo: TaskProductInfo,
                   materialNumber: String = productInfo.materialNumber,
                   description: String = productInfo.description,
                   uom: Uom = productInfo.uom,
                   type:ProductType = productInfo.type,
                   isSet: Boolean = productInfo.isSet,
                   sectionId: String = productInfo.sectionId,
                   matrixType: MatrixType = productInfo.matrixType,
                   materialType: String = productInfo.materialType,
                   placeCode: String = productInfo.placeCode,
                   factCount: Double = productInfo.factCount,
                   isPositionCalc: Boolean = productInfo.isPositionCalc,
                   isDel: Boolean = productInfo.isDel,
                   isExcOld: Boolean = productInfo.isExcOld) : TaskProductInfo {
            return TaskProductInfo(
                            materialNumber = materialNumber,
                            description = description,
                            uom = uom,
                            type = type,
                            isSet = isSet,
                            sectionId = sectionId,
                            matrixType = matrixType,
                            materialType = materialType,
                            placeCode = placeCode,
                            factCount = factCount,
                            isPositionCalc = isPositionCalc,
                            isDel = isDel,
                            isExcOld = isExcOld
            )
        }
    }

}