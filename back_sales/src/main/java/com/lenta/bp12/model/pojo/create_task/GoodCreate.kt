package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.model.MarkType
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.Part
import com.lenta.bp12.model.pojo.Position
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom

class GoodCreate(
        ean: String,
        eans: List<String> = emptyList(),
        material: String,
        name: String,
        kind: GoodKind,
        section: String,
        matrix: MatrixType,
        volume: Double,
        control: ControlType = ControlType.COMMON,
        commonUnits: Uom = Uom.ST,
        innerUnits: Uom = Uom.ST,
        innerQuantity: Double = 0.0,
        producers: MutableList<ProducerInfo> = mutableListOf(),
        positions: MutableList<Position> = mutableListOf(),
        marks: MutableList<Mark> = mutableListOf(),
        parts: MutableList<Part> = mutableListOf(),
        markType: MarkType,
        maxRetailPrice: String,

        val type: String,

        val providers: MutableList<ProviderInfo> = mutableListOf()

) : Good(ean, eans, material, name, kind, section, matrix, volume, control, commonUnits, innerUnits, innerQuantity, producers, positions, marks, parts, markType, maxRetailPrice)