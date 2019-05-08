package com.lenta.shared.utilities.extentions

fun Iterable<String>.toSQliteSet(): String {
    return this.joinToString(prefix = "(", separator = ",", postfix = ")") { "'$it'" }
}