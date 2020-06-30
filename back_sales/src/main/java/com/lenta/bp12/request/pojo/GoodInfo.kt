package com.lenta.bp12.request.pojo

import com.lenta.bp12.model.GoodType
import com.lenta.shared.models.core.MatrixType

data class GoodInfo(
        val ean: String,
        val material: String,
        val name: String,
        val section: String,
        val matrix: MatrixType,
        val type: GoodType
)