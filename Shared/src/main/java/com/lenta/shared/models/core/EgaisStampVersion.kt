package com.lenta.shared.models.core

import com.lenta.shared.platform.constants.Constants.MARK_150
import com.lenta.shared.platform.constants.Constants.MARK_68

enum class EgaisStampVersion(val version: Int) {
    UNKNOWN(0),
    V1(1),
    V2(MARK_68),
    V3(MARK_150)
}