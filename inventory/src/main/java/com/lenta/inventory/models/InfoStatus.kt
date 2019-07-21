package com.lenta.inventory.models

enum class InfoStatus(val status: Int) {
    StampFound (1),
    StampOverload (2),
    StampOfOtherProduct (3),
    StampNotFound (4),
    BoxFound (100),
    BoxWithProblem(101),
    BatchFound (200),
    BatchNotFound (201)
}