package com.lenta.shared.models.core

open class ExciseStamp(val materialNumber: String, val code: String) : IExciseStamp {
    override fun egaisVersion(): Int {
        return getEgaisVersion(code)
    }

    companion object {
        fun getEgaisVersion(code: String): Int {
            when (code.length) {
                EgaisStampVersion.V2.version -> return EgaisStampVersion.V2.version
                EgaisStampVersion.V3.version -> return EgaisStampVersion.V3.version
                else -> return EgaisStampVersion.UNKNOWN.version
            }
        }
    }
}