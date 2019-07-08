package com.lenta.inventory.models

import com.lenta.inventory.R

enum class RecountType(val recountType: String) {
    None(""),
    Simple("1"),
    ParallelByStorePlaces("2"),
    ParallelByPerNo("4")
}


fun RecountType.getDescriptionStringRes(): Int {
    return when (this) {
        RecountType.Simple -> R.string.simple_recount
        RecountType.ParallelByStorePlaces -> R.string.parallel_by_storeplaces
        RecountType.ParallelByPerNo -> R.string.parallel_by_per_number
        else -> R.string.not_selected
    }
}
