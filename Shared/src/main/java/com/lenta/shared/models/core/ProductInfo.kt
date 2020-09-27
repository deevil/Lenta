package com.lenta.shared.models.core

open class ProductInfo(
        val materialNumber: String,
        val description: String,
        val uom: Uom,
        val type: ProductType,
        val isSet: Boolean,
        val sectionId: String,
        val matrixType: MatrixType,
        val materialType: String,
        val markedGoodType:  String = ""
) : IProduct {

    override fun getMaterialLastSix(): String {
        return if (materialNumber.length > 6)
            materialNumber.substring(materialNumber.length - 6)
        else
            materialNumber
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as ProductInfo
        return equals(other)
    }

    fun equals(pi: ProductInfo?): Boolean {
        return if (pi == null) {
            false
        } else pi.materialNumber == materialNumber
                && pi.uom.equals(uom)
                && pi.type == type
                && pi.isSet == isSet
    }

    override fun hashCode(): Int {
        return materialNumber.hashCode() * 197 +
                uom.hashCode() * 197 +
                type.hashCode() * 197 +
                isSet.hashCode() * 197
    }

}