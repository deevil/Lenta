package com.lenta.bp16.platform.converter

interface IConvertable<out T> {
    fun convert(): T
}