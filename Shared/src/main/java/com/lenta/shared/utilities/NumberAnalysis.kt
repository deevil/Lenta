package com.lenta.shared.utilities

import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.getMaterialInCommonFormat

fun actionByNumber(
        number: String,
        funcForEan: (ean: String) -> Unit,
        funcForMaterial: (material: String) -> Unit,
        funcForSapOrBar: ((sapCallback: () -> Unit, barCallback: () -> Unit) -> Unit)?,
        funcForExcise: ((exciseNumber: String) -> Unit)? = null,
        funcForBox: ((boxNumber: String) -> Unit)? = null,
        funcForMark: ((markNumber: String) -> Unit)? = null,
        funcForNotValidFormat: () -> Unit
) {

    val numberInfo = ScanCodeInfo(number)
    val numberLength = number.length

    Logg.d { "--> checked number = $numberLength / $number" }

    if (numberInfo.isEnterCodeValid) {
        when (numberLength) {
            Constants.SAP_6 -> funcForMaterial(getMaterialInCommonFormat(number))
            Constants.SAP_18 -> funcForMaterial(number)
            Constants.SAP_OR_BAR_12 -> {
                funcForSapOrBar?.invoke(
                        { funcForMaterial(getMaterialInCommonFormat(number)) },
                        { funcForEan(numberInfo.eanWithoutWeight) }
                ) ?: funcForNotValidFormat()
            }
            else -> funcForEan(numberInfo.eanWithoutWeight)
        }
    } else {
        when (numberLength) {
            Constants.MARK_150, Constants.MARK_68 -> {
                funcForExcise?.invoke(number) ?: funcForNotValidFormat()
            }
            Constants.MARK_134, Constants.MARK_39 -> {
                funcForMark?.invoke(number) ?: funcForNotValidFormat()
            }
            Constants.BOX_26 -> {
                funcForBox?.invoke(number) ?: funcForNotValidFormat()
            }
            else -> funcForNotValidFormat()
        }
    }
}