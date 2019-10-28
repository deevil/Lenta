package com.lenta.shared.models.core

interface StateFromToString {
    fun saveStateToString(): String
    fun getStateFromString(state: String)
    fun restoreData(data: Any)
}