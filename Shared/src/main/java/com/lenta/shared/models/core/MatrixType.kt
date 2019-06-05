package com.lenta.shared.models.core

enum class MatrixType {
    Unknown,
    Active,
    Passive,
    Deleted
}

fun getMatrixType(code: String): MatrixType {
    return when (code) {
        "A" -> MatrixType.Active
        "P" -> MatrixType.Passive
        "D" -> MatrixType.Deleted
        else -> MatrixType.Unknown
    }
}

fun MatrixType?.isNormal(): Boolean {
    return this == MatrixType.Active || this == MatrixType.Passive
}