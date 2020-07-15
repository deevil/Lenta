package com.lenta.shared.utilities.extentions

import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo

fun ScanCodeInfo.getQuantity(units: Uom): Double {
    return quantity.takeIf { units.code == Uom.G.code } ?: quantity.div(1000)
}

fun ScanCodeInfo.getConvertedQuantity(divider: Double): Double {
    return quantity.div(divider)
}