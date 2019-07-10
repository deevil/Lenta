package com.lenta.bp7.data

enum class StoreRetailType(val type: String) {
    HYPER("H"),
    SUPER("S"),
}

enum class Enabled(val type: String) {
    YES("YES"),
    NO("NO"),
}

enum class CheckType(val type: String) {
    SELF_CONTROL("SELF_CONTROL"),
    EXTERNAL_AUDIT("EXTERNAL_AUDIT"),
}