package com.lenta.bp14.features.price_check.price_scanner

import com.lenta.shared.platform.viewmodel.CoreViewModel

class PriceScannerViewModel : CoreViewModel() {
    fun getTitle(): String {
        return "???"
    }

    fun isErrorCode(code: String): Boolean? {
        return when {
            code.contains("7") -> false
            code.toCharArray().all { char -> char.isDigit() } -> true
            else -> null
        }
    }

}
