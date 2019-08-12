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
                      val isAddedManually: Boolean = false,
                      val isExcOld: Boolean) : ProductInfo(materialNumber, description, uom, type, isSet, sectionId, matrixType, materialType) {

    fun getDisplayName() : String {
        return "${getMaterialLastSix()} ${description}"
    }

    fun getFormattedCount() : String {
        return "${factCount.toStringFormatted()} ${uom.name}"
    }

    fun copy(materialNumber: String = this.materialNumber,
            description: String = this.description,
            uom: Uom = this.uom,
            type:ProductType = this.type,
            isSet: Boolean = this.isSet,
            sectionId: String = this.sectionId,
            matrixType: MatrixType = this.matrixType,
            materialType: String = this.materialType,
            placeCode: String = this.placeCode,
            factCount: Double = this.factCount,
            isPositionCalc: Boolean = this.isPositionCalc,
            isDel: Boolean = this.isDel,
            isExcOld: Boolean = this.isExcOld) : TaskProductInfo {
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