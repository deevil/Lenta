package com.lenta.shared.utilities

import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo

fun actionByNumber(
        number: String,
        funcForEan: ((ean: String) -> Unit)? = null,
        funcForMaterial: ((material: String) -> Unit)? = null,
        funcForSapOrBar: ((sapCallback: () -> Unit, barCallback: () -> Unit) -> Unit)? = null,
        funcForExcise: ((exciseNumber: String) -> Unit)? = null,
        funcForExciseBox: ((boxNumber: String) -> Unit)? = null,
        funcForMark: ((markNumber: String) -> Unit)? = null,
        funcForShoes: ((ean: String, correctedNumber: String, originalNumber: String) -> Unit)? = null,
        funcForCigarettes: ((markNumber: String) -> Unit)? = null,
        funcForCigaretteBox: ((markNumber: String) -> Unit)? = null,
        funcForNotValidFormat: () -> Unit
) {

    val numberInfo = ScanCodeInfo(number)
    val numberLength = number.length

    Logg.d { "--> checked number = $numberLength / $number" }

    if (numberInfo.isEnterCodeValid) {
        when (numberLength) {
            Constants.SAP_6 -> funcForMaterial?.invoke(getMaterialInCommonFormat(number))
            Constants.SAP_18 -> funcForMaterial?.invoke(number)
            Constants.SAP_OR_BAR_12 -> {
                funcForSapOrBar?.invoke(
                        { funcForMaterial?.invoke(getMaterialInCommonFormat(number)) },
                        { funcForEan?.invoke(numberInfo.eanWithoutWeight) }
                ) ?: funcForNotValidFormat()
            }
            else -> funcForEan?.invoke(numberInfo.eanWithoutWeight)
        }

        return
    }

    if (isShoesMark(number)) {
        val matchResult = Regex(Constants.SHOES_MARK_PATTERN).find(number)
        matchResult?.let {
            val (barcode, _, _, _, _, _) = it.destructured // barcode, gtin, serial, tradeCode, verificationKey, verificationCode
            barcode
        }?.let { ean ->
            val correctedNumber = number // todo Нужно отбросить криптохвост. Как?

            funcForShoes?.invoke(ean, correctedNumber, number) ?: funcForNotValidFormat()
        } ?: funcForNotValidFormat()

        return
    }

    if (isCigarettesMark(number)) {
        funcForCigarettes?.invoke(number) ?: funcForNotValidFormat()
        return
    }

    if (isCigarettesBox(number)) {
        funcForCigaretteBox?.invoke(number) ?: funcForNotValidFormat()
        return
    }

    when (numberLength) {
        Constants.EXCISE_MARK_150, Constants.EXCISE_MARK_68 -> {
            funcForExcise?.invoke(number) ?: funcForNotValidFormat()
        }
        Constants.MARK_134, Constants.MARK_39 -> {
            funcForMark?.invoke(number) ?: funcForNotValidFormat()
        }
        Constants.EXCISE_BOX_26 -> {
            funcForExciseBox?.invoke(number) ?: funcForNotValidFormat()
        }
        else -> funcForNotValidFormat()
    }

}