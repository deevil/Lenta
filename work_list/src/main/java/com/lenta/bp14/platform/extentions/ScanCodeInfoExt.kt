package com.lenta.bp14.platform.extentions

import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo


fun ScanCodeInfo.getQuantity(defaultUnits: Uom): Double {
    return if (defaultUnits.code == Uom.G.code) quantity / 1000 else quantity
}