package com.lenta.movement.models

data class TaskSettings(
    val description: String,
    val shipmentStorageList: List<String>,
    val signsOfDiv: Set<GoodsSignOfDivision>
)