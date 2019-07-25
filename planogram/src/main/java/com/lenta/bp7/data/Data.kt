package com.lenta.bp7.data

enum class StoreRetailType(val type: String) {
    HYPER("H"),
    SUPER("S"),
}

enum class Enabled(val type: String) {
    YES("YES"),
    NO("NO"),
}

enum class CheckType {
    NOT_DEFINED,
    SELF_CONTROL,
    EXTERNAL_AUDIT,
}