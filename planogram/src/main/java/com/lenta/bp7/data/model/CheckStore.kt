package com.lenta.bp7.data.model

class CheckStore(
        val storeNumber: String,
        val segments: MutableList<Segment> = mutableListOf()
)