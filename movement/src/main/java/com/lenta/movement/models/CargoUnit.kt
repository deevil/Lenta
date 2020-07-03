package com.lenta.movement.models

data class CargoUnit(
        val number : String,
        val eoList: List<ProcessingUnit>
)