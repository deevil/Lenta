package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom

data class Good(
        val ean: String,
        val material: String,
        val name: String,
        var quantity: Double = 0.0,
        val innerQuantity: Double,
        val units: Uom,
        val orderUnits: Uom,
        val kind: GoodKind,
        val type: String,
        val control: ControlType,
        val section: String,
        val matrix: MatrixType,
        val providers: MutableList<ProviderInfo>,
        val producers: MutableList<ProducerInfo>,
        val provider: ProviderInfo? = null,
        val producer: ProducerInfo? = null,
        val marks: MutableList<Mark> = mutableListOf(),
        val parts: MutableList<Part> = mutableListOf(),
        var isCounted: Boolean = false,
        var isDelete: Boolean = false
) {

    fun getNameWithMaterial(delimiter: String = " "): String {
        return "${material.takeLast(6)}$delimiter$name"
    }

    fun isBox(): Boolean {
        return innerQuantity > 0
    }

}