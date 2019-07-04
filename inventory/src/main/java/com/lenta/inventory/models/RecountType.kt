package com.lenta.inventory.models

enum class RecountType(val recountType: String) {
    None(""),
    Simple ("1"),
    ParallelByStorePlaces("2"),
    ParallelByPerNo("4")
}