package com.lenta.inventory.models

enum class InfoStatus(val status: String) {
    StampFound ("01"),
    StampOverload ("02"),
    StampOfOtherProduct ("03"),
    StampNotFound ("04"),
    BoxFound ("100"),
    BoxWithProblem("101"),
    BatchFound ("200"),
    BatchNotFound ("201")
}