package com.lenta.movement.models

data class TaskSettings(
    val description: String,
    val shipmentStorage: String,
    val signsOfDiv: Set<GoodsSignOfDivision>
)