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
        funcForShoes: ((ean: String, markWithoutTail: String) -> Unit)? = null,
        funcForCigarettes: ((markWithoutTail: String) -> Unit)? = null,
        funcForCigaretteBox: ((markWithoutTail: String) -> Unit)? = null,
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
        runCatching {
            val matchResult = Regex(Constants.SHOES_MARK_PATTERN).find(number)
            matchResult?.let {
                val (markWithoutTail, ean, _, _, _, _) = it.destructured
                Logg.d { "--> Shoes mark without tail = $markWithoutTail" }
                funcForShoes?.invoke(ean, markWithoutTail) ?: funcForNotValidFormat()
            } ?: funcForNotValidFormat()
        }.onFailure {
            Logg.e { "e: $it" }
        }

        return
    }

    if (isCigarettesMark(number)) {
        runCatching {
            val matchResult = Regex(Constants.CIGARETTES_MARK_PATTERN).find(number)
            matchResult?.let {
                val (markWithoutTail, _, _, _, _, _) = it.destructured
                Logg.d { "--> Cigarette mark without tail = $markWithoutTail" }
                funcForCigarettes?.invoke(markWithoutTail) ?: funcForNotValidFormat()
            } ?: funcForNotValidFormat()
        }.onFailure {
            Logg.e { "e: $it" }
        }

        return
    }

    if (isCigarettesBox(number)) {
        runCatching {
            val matchResult = Regex(Constants.CIGARETTES_BOX_PATTERN).find(number)
            matchResult?.let {
                val (markWithoutTail, _, _, _, _, _) = it.destructured
                Logg.d { "--> Cigarette box mark without tail = $markWithoutTail" }
                funcForCigaretteBox?.invoke(markWithoutTail) ?: funcForNotValidFormat()
            } ?: funcForNotValidFormat()
        }.onFailure {
            Logg.e { "e: $it" }
        }

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