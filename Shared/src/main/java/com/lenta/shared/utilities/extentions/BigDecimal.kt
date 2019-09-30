package com.lenta.shared.utilities.extentions

import java.math.BigDecimal


fun BigDecimal.dropTail(): String {
    return this.toString().dropWhile { it == '0' || it == '.' }
}