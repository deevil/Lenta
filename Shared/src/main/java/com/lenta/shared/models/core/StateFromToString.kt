package com.lenta.shared.models.core

interface StateFromToString {
    fun saveStateToString(): String
    fun loadStateFromString(state: String)
}