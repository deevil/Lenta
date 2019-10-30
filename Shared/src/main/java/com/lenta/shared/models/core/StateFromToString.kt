package com.lenta.shared.models.core

interface StateFromToString {
    fun getStateAsString(): String
    fun loadStateFromString(state: String)
}