package com.lenta.inventory.models

enum class StorePlaceLockMode(val mode: String) {
    None(""),
    Lock ("1"),
    Unlock("2"),
    Check("3")
}