package com.lenta.shared.utilities.extentions

fun Iterable<String>.toSQliteSet(): String {
    return joinToString(prefix = "(", separator = ",", postfix = ")") { "'$it'" }
}