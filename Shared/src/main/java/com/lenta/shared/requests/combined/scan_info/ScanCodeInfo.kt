package com.lenta.shared.requests.combined.scan_info

import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.requests.combined.scan_info.pojo.EanInfo
import java.lang.Long.parseLong
import java.util.*

class ScanCodeInfo(
        private val originalNumber: String,
        private val fixedQuantity: Double? = null
) {

    private val prefix by lazy {
        originalNumber.take(2)
    }

    private val withWeight by lazy {
        originalNumber.length == 13 && (prefix in arrayOf("23", "24", "27", "28"))
    }

    private val withWeightInTens by lazy {
        withWeight && prefix == "27"
    }

    private val quantity: Double by lazy {
        if (withWeight) {
            originalNumber.takeLast(6).dropLast(1).toDoubleOrNull() ?: 0.0.let { weight ->
                if (withWeightInTens) weight * 10 else weight
            }
        } else 1.0
    }

    val eanWithoutWeight: String by lazy {
        if (withWeight) "${originalNumber.dropLast(6)}000000" else originalNumber
    }

    private val isMaterialNumber: Boolean by lazy {
        originalNumber.length.let { it == 6 || it == 18 }
    }

    val eanNumberForSearch: String? by lazy {
        if (!isMaterialNumber) eanWithoutWeight else null
    }

    val materialNumberForSearch: String? by lazy {
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

    fun getQuantity(defaultUnits: Uom): Double {
        return if (defaultUnits.code == Uom.G.code) quantity / 1000 else quantity
    }

    private fun isEAN128Valid(code: String): Boolean {
        if (code.startsWith("(01)") || code.startsWith("(02)")) {
            val subCode = code.substring(3, code.length)
            return !(subCode.length != 14 && !subCode.matches("[0-9]+".toRegex()))

        }
        return false
    }
}


fun analyseCode(
        code: String,
        funcForEan: (eanCode: String) -> Unit,
        funcForMatNr: (matNumber: String) -> Unit,
        funcForSapOrBar: ((sapCallback: () -> Unit, barCallback: () -> Unit) -> Unit)?,
        funcForPriceQrCode: ((matNumber: String) -> Unit)? = null,
        funcForExciseCode: ((exciseCode: String) -> Unit)? = null,
        funcForMarkCode: ((markCode: String) -> Unit)? = null,
        funcForNotValidFormat: () -> Unit
) {

    code.length.let { length ->

        if (code.startsWith("(01)")) {
            funcForPriceQrCode?.let {
                it(code)
                return
            }
            funcForNotValidFormat()
            return
        }

        if (length == Constants.EXCISE_FULL_CODE || length == Constants.EXCISE_SIMPLE_CODE) {
            funcForExciseCode?.let {
                it(code)
                return
            }
            funcForNotValidFormat()
            return
        }

        if (length == Constants.MARKED_FULL_CODE || length == Constants.MARKED_SIMPLE_CODE) {
            funcForMarkCode?.let {
                it(code)
                return
            }
            funcForNotValidFormat()
            return
        }

        if (length < Constants.COMMON_SAP_LENGTH) {
            funcForNotValidFormat()
            return
        }

        if (length >= Constants.COMMON_SAP_LENGTH) {
            when (length) {
                Constants.COMMON_SAP_LENGTH -> funcForMatNr("000000000000${code.takeLast(6)}")
                Constants.COMMON_SAP_FULL_LENGTH -> funcForMatNr(code)
                Constants.SAP_OR_BAR_LENGTH -> {
                    if (funcForSapOrBar == null) {
                        funcForNotValidFormat()
                    } else {
                        funcForSapOrBar(
                                { funcForMatNr(code) },
                                { funcForEan(code) }
                        )
                    }

                }
                else -> funcForEan(code)
            }
        }
    }

}