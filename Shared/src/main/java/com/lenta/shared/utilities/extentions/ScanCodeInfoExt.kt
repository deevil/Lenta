package com.lenta.shared.utilities.extentions

import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo

fun ScanCodeInfo.getQuantity(units: Uom): Double {
    return if (units.code == Uom.G.code) quantity / 1000 else quantity
}