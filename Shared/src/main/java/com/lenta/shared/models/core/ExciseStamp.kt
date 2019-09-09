package com.lenta.shared.models.core

open class ExciseStamp(val materialNumber: String, val code: String) : IExciseStamp {
    override fun egaisVersion(): Int {
        return getEgaisVersion(code)
    }

    companion object {
        fun getEgaisVersion(code: String): Int {
            return when (code.length) {
                // Все марки определяем как 150
                EgaisStampVersion.V2.version,
                EgaisStampVersion.V3.version -> EgaisStampVersion.V3.version
                else -> EgaisStampVersion.UNKNOWN.version
            }
        }
    }
}