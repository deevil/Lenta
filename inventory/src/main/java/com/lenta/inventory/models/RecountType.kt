package com.lenta.inventory.models

enum class RecountType(val recountType: String) {
    None(""),
    Simple ("1"),
    ParallelByStorplaces("2"),
    ParallelByPerNo("4")
}