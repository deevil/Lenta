package com.lenta.movement.models

import com.lenta.shared.models.core.GisControl

data class TaskSettings(
    val description: String,
    val shipmentStorageList: List<String>,
    val signsOfDiv: Set<GoodsSignOfDivision>,
    val gisControls: Set<GisControl>
)