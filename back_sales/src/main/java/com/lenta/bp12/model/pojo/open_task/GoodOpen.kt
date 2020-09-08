package com.lenta.bp12.model.pojo.open_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.model.MarkType
import com.lenta.bp12.model.pojo.*
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom

class GoodOpen(
        ean: String,
        eans: List<String> = emptyList(),
        material: String,
        name: String,
        kind: GoodKind,
        control: ControlType = ControlType.COMMON,
        section: String,
        matrix: MatrixType,
        volume: Double,
        commonUnits: Uom = Uom.ST,
        innerUnits: Uom = Uom.ST,
        innerQuantity: Double = 0.0,
        producers: MutableList<ProducerInfo> = mutableListOf(),
        positions: MutableList<Position> = mutableListOf(),
        marks: MutableList<Mark> = mutableListOf(),
        parts: MutableList<Part> = mutableListOf(),
        markType: MarkType,
        markTypeGroup: MarkTypeGroup?,
        maxRetailPrice: String,

        val planQuantity: Double = 0.0,
        val factQuantity: Double = 0.0,
        var isCounted: Boolean = false,
        var isDeleted: Boolean = false,
        val provider: ProviderInfo
) : Good(ean, eans, material, name, kind, section, matrix, volume, control, commonUnits, innerUnits, innerQuantity, producers, positions, marks, parts, markType, markTypeGroup, maxRetailPrice)