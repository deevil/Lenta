package com.lenta.shared.utilities.extentions

import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo

fun ScanCodeInfo.getQuantity(units: Uom): Double {
    return weight.takeIf { units.code == Uom.G.code } ?: weight.div(1000)
}

fun ScanCodeInfo.getConvertedQuantity(divider: Double): Double {
    return weight.div(divider)
}