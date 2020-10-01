package com.lenta.shared.requests.combined.scan_info

import com.lenta.shared.requests.combined.scan_info.pojo.EanInfo
import com.lenta.shared.utilities.extentions.unsafeLazy
import java.lang.Long.parseLong
import java.util.*

class ScanCodeInfo(
        private val originalNumber: String,
        private val fixedQuantity: Double? = null
) {

    private val prefix by unsafeLazy {
        originalNumber.take(2)
    }

    private val withWeight by unsafeLazy {
        originalNumber.length == 13 && (prefix in arrayOf("23", "24", "27", "28"))
    }

    private val withWeightInTens by unsafeLazy {
        withWeight && prefix == "27"
    }

    val weight: Double by unsafeLazy {
        if (withWeight) {
            originalNumber.takeLast(6).dropLast(1).toDoubleOrNull()?.let { weight ->
                if (withWeightInTens) weight * 10 else weight
            } ?: 0.0
        } else {
            1.0
        }
    }

    val eanWithoutWeight: String by unsafeLazy {
        if (withWeight) "${originalNumber.dropLast(6)}000000" else originalNumber
    }

    private val isMaterialNumber: Boolean by unsafeLazy {
        originalNumber.length.let { it == 6 || it == 18 }
    }

    val eanNumberForSearch: String? by unsafeLazy {
        if (!isMaterialNumber) eanWithoutWeight else null
    }

    val materialNumberForSearch: String? by unsafeLazy {
        if (isMaterialNumber) {
            String.format(Locale.US, "%018d", parseLong(originalNumber))
        } else {
            null
        }
    }

    val isEnterCodeValid by lazy {
        var res = false
        if (originalNumber.length == 6 || originalNumber.length == 8
                || originalNumber.length == 12 || originalNumber.length == 13
                || originalNumber.length == 14 || originalNumber.length == 16
                || isMaterialNumber) {
            res = true
        } else if (originalNumber.length > 16) {
            res = isEAN128Valid(originalNumber)
        }
        res
    }

    fun extractQuantityFromEan(eanInfo: EanInfo?): Double {
        fixedQuantity?.let {
            return it
        }
        var quantity = 1.0
        eanInfo?.let {
            if (it.ean != originalNumber) {
                quantity = originalNumber.takeLast(6).dropLast(1).toDoubleOrNull() ?: 0.0
            }
            quantity = quantity * it.umrez / it.umren
        }
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