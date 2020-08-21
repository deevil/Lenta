package com.lenta.bp14.models.ui

import com.lenta.bp14.models.data.GoodType
import com.lenta.shared.models.core.MatrixType

data class OptionsUi(
        val matrixType: MatrixType,
        val goodType: GoodType,
        val section: String,
        val healthFood: Boolean,
        val novelty: Boolean
)