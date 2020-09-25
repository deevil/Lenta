package com.lenta.bp15.features.good_info

import com.lenta.shared.models.core.MatrixType

data class GoodInfoUi(
        val markType: String,
        val matrix: MatrixType,
        val section: String
)