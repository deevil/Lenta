package com.lenta.bp14.features.price_check.price_scanner

import com.lenta.bp14.ml.CheckStatus
import com.lenta.shared.platform.viewmodel.CoreViewModel

class PriceScannerViewModel : CoreViewModel() {
    fun getTitle(): String {
        return "???"
    }

    fun checkStatus(code: String): CheckStatus? {
        return when {
            code.contains("7") -> CheckStatus.VALID
            code.toCharArray().all { char -> char.isDigit() } -> CheckStatus.NOT_VALID
            else -> CheckStatus.UNKNOWN
        }
    }

}
