package com.lenta.shared.models.core

class Uom(val code: String, val name: String) {
    companion object {
        val DEFAULT = Uom("ST", "шт")
    }

    override fun toString(): String {
        return name
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Uom

        return equals(other)
    }

    fun equals(uom: Uom?): Boolean {
        return if (uom == null) false else code == uom.code
    }
}