package com.lenta.shared.utilities


enum class PackageName(val path: String) {
    PLE("com.lenta.bp7"),
    WOB("com.lenta.bp10"),
    PRO("com.lenta.bp16"),
    INV("com.lenta.inventory"),
    SHA("com.lenta.shared"),
    OPP("com.lenta.bp18"),
    GRZ("com.lenta.bp9")
}

enum class TabIndicatorColor {
    YELLOW,
    RED
}

enum class BlockType {
    SELF_LOCK,
    LOCK,
    UNLOCK;

    companion object {
        fun from(code: String): BlockType {
            return when (code) {
                "1" -> SELF_LOCK
                "2" -> LOCK
                else -> UNLOCK
            }
        }
    }
}