package com.lenta.bp7.data.model

import com.lenta.shared.utilities.Logg

class CheckStoreData(
        val segments: MutableList<Segment> = mutableListOf()
) {

    init {
        createTestData()
    }

    private fun createTestData() {
        Logg.d { "Creation test data for CheckStoreData." }
    }
}