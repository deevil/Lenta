package com.lenta.bp10.features.good_information

class LimitsChecker(private val limit: Double, private val observer: () -> Unit) {

    private var _wasExceeded = false


    fun check(value: Double) {
        if (limit != 0.0 && !_wasExceeded && value > limit) {
            _wasExceeded = true
            observer()
        }
    }

    fun wasExceeded() = _wasExceeded


}

