package com.lenta.shared.requests.combined.scan_info

import com.lenta.shared.requests.combined.scan_info.pojo.EanInfo
import java.util.*

class ScanCodeInfo(val originalNumber: String, val fixedQuantity: Double?) {

    val codeWithoutQuantity by lazy {
        "${originalNumber.dropLast(6)}000000"
    }

    val withQuantity by lazy {
        val prefix = originalNumber.take(2)
        originalNumber.length == 13 && (prefix in arrayOf("23", "24", "27", "28"))
    }

    val isMaterialNumber: Boolean? by lazy {
        originalNumber.length == 6 || originalNumber.length == 18
    }

    val isUnknownNumber: Boolean by lazy {
        isMaterialNumber == null
    }

    val eanNumberForSearch: String? by lazy {
        if (isUnknownNumber) {
            null
        } else if (isMaterialNumber == false) {
            if (withQuantity) {
                codeWithoutQuantity
            } else {
                originalNumber
            }
        } else {
            null
        }
    }

    val materialNumberForSearch: String? by lazy {
        if (isMaterialNumber == true) {
            String.format(Locale.US, "%018d", java.lang.Long.parseLong(originalNumber))
        } else {
            null
        }
    }

    val isEnterCodeValid by lazy {
        var res = false
        if (originalNumber.length == 6 || originalNumber.length == 8
                || originalNumber.length == 12 || originalNumber.length == 13
                || originalNumber.length == 14 || originalNumber.length == 16
                || isMaterialNumber == true) {
            res = true
        } else if (originalNumber.length > 16) {
            res = isEAN128Valid(originalNumber)
        }
        res
    }

    fun extractQuantityFromEan(eanInfo: EanInfo): Double {
        fixedQuantity?.let {
            return it
        }
        var quantity = 1.0
        if (eanInfo.ean != originalNumber) {
            quantity = originalNumber.takeLast(6).dropLast(1).toDoubleOrNull() ?: 0.0

        }
        quantity = quantity * eanInfo.umrez / eanInfo.umren
        return quantity
    }

    private fun isEAN128Valid(code: String): Boolean {
        if (code.startsWith("(01)") || code.startsWith("(02)")) {
            val subCode = code.substring(3, code.length)
            return !(subCode.length != 14 && !subCode.matches("[0-9]+".toRegex()))

        }
        return false
    }
}