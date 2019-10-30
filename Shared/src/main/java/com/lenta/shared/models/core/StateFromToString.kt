package com.lenta.shared.models.core

interface StateFromToString {
    fun gatStateAsString(): String
    fun loadStateFromString(state: String)
}