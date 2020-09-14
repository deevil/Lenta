package com.lenta.bp12.model

import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.constants.Constants.TOBACCO_BOX_MARK_RANGE_21_28
import com.lenta.shared.platform.constants.Constants.TOBACCO_MARK_BLOCK_OR_BOX_RANGE_30_44
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.*

fun actionByNumber(
        number: String,
        actionFromGood: Boolean? = null,
        funcForEan: ((ean: String) -> Unit)? = null,
        funcForMaterial: ((material: String) -> Unit)? = null,
        funcForSapOrBar: ((sapCallback: () -> Unit, barCallback: () -> Unit) -> Unit)? = null,
        funcForExcise: ((exciseNumber: String) -> Unit)? = null,
        funcForBox: ((boxNumber: String) -> Unit)? = null,
        funcForMark: ((markNumber: String) -> Unit)? = null,
        funcForNotValidBarFormat: () -> Unit
) {
    val numberLength = number.length

    val numberInfo = ScanCodeInfo(number)

    Logg.d { "--> checked number = $numberLength / $number" }

    if (numberInfo.isEnterCodeValid) {
        when (numberLength) {
            Constants.SAP_6 -> {
                val materialInCommonFormat = getMaterialInCommonFormat(number)
                funcForMaterial?.invoke(materialInCommonFormat) ?: funcForNotValidBarFormat()
            }
            Constants.SAP_18 -> {
                if (actionFromGood != null && actionFromGood) {
                    funcForBox?.invoke(number) ?: funcForNotValidBarFormat
                }
                else funcForMaterial?.invoke(number) ?: funcForNotValidBarFormat()
            }
            Constants.SAP_OR_BAR_12 -> {
                funcForSapOrBar?.invoke({
                    val materialInCommonFormat = getMaterialInCommonFormat(number)
                    funcForMaterial?.invoke(materialInCommonFormat)
                            ?: funcForNotValidBarFormat()
                }, {
                    funcForEan?.invoke(numberInfo.eanWithoutWeight)
                            ?: funcForNotValidBarFormat()
                }
                ) ?: funcForNotValidBarFormat()
            }
            else -> {
                funcForEan?.invoke(numberInfo.eanWithoutWeight)
                        ?: funcForNotValidBarFormat()
            }
        }
    } else {
        if (isShoesMark(number)) {
            funcForMark?.invoke(number)
        } else when (numberLength) {
            Constants.EXCISE_MARK_150, Constants.EXCISE_MARK_68 -> {
                funcForExcise?.invoke(number) ?: funcForNotValidBarFormat()
            }
            in TOBACCO_BOX_MARK_RANGE_21_28 -> {
                funcForBox?.invoke(number) ?: funcForNotValidBarFormat()
            }
            Constants.MARK_TOBACCO_PACK_29,
            in TOBACCO_MARK_BLOCK_OR_BOX_RANGE_30_44 -> {
                funcForMark?.invoke(number) ?: funcForNotValidBarFormat()
            }
            else -> {
                if (isCigarettesBox(number)) {
                    funcForMark?.invoke(number) ?: funcForNotValidBarFormat()
                } else {
                    funcForNotValidBarFormat()
                }
            }
        }
    }
}