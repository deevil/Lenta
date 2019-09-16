package com.lenta.shared.models.core

interface StateFromToString {
    fun stateToString(): String
    fun stateFromString(state: String)
}