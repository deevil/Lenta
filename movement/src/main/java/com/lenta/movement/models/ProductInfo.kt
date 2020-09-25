package com.lenta.movement.models

import com.lenta.shared.models.core.*
import com.lenta.shared.models.core.ProductInfo

open class ProductInfo(
        materialNumber: String,
        description: String,
        uom: Uom,
        type: ProductType,
        isSet: Boolean,
        sectionId: String,
        matrixType: MatrixType,
        //Вид товара
        materialType: String,
        /** Группа закупок */
        val ekGroup: String,
        /** Группа товаров */
        val matkl: String,
        val volume: Double,
        val quantityInvestments: Int,
        val suppliers: List<Supplier>,
        val isRus: Boolean,
        val isVet: Boolean,
        val isFood: Boolean

) : ProductInfo(materialNumber, description, uom, type, isSet, sectionId, matrixType, materialType) {

    val isAlco: Boolean
        get() = type != ProductType.General

    val isExcise: Boolean
        get() = type == ProductType.ExciseAlcohol

    val isNotExcise: Boolean
        get() = type == ProductType.NonExciseAlcohol

    val isUsual: Boolean
        get() = isVet.not() && isAlco.not()

    val isMarked: Boolean
        get() = type == ProductType.Marked

}