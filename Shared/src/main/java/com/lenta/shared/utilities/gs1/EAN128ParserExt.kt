package com.lenta.shared.utilities.gs1

import java.util.*

fun Map<EAN128Parser.AII, String>.getString(identifier: ApplicationIdentifier): String {
    return this.entries.find { it.key.AI == identifier.key }?.value.orEmpty()
}

fun Map<EAN128Parser.AII, String>.getLong(identifier: ApplicationIdentifier): Long {
    return getString(identifier).toLong()
}

fun Map<EAN128Parser.AII, String>.getDouble(identifier: ApplicationIdentifier): Double {
    return getString(identifier).toDouble()
}

fun Map<EAN128Parser.AII, String>.getDate(identifier: ApplicationIdentifier): Date? {
    val value = getString(identifier)
    return ElementStrings.SequenceReader.parseDateAndTime(value)
}