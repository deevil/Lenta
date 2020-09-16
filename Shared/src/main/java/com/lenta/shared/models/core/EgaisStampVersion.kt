package com.lenta.shared.models.core

import com.lenta.shared.platform.constants.Constants.EXCISE_MARK_150
import com.lenta.shared.platform.constants.Constants.EXCISE_MARK_68

enum class EgaisStampVersion(val version: Int) {
    UNKNOWN(0),
    V1(1),
    V2(EXCISE_MARK_68),
    V3(EXCISE_MARK_150)
}