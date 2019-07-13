package com.lenta.bp7.data.model

import com.lenta.shared.utilities.Logg

class CheckStoreData(
        val segments: MutableList<Segment> = mutableListOf()
) {

    init {
        generateTestData()
    }

    private fun generateTestData() {
        Logg.d { "Test data generation for CheckStoreData" }

        for (i in 1..15) {
            segments.add(Segment(
                    id = i,
                    number = createSegmentNumber(),
                    storeNumber = "0007",
                    status = createSegmentStatus(),
                    shelves =  createShelvesList()))
        }
    }

    private fun createSegmentNumber(): String {
        return (100..999).random().toString() + "-" + (100..999).random().toString()
    }

    private fun createSegmentStatus(): SegmentStatus {
        return when ((1..4).random()) {
            1 -> SegmentStatus.STARTED
            2 -> SegmentStatus.UNFINISHED
            3 -> SegmentStatus.PROCESSED
            else -> SegmentStatus.DELETED
        }
    }

    private fun createShelvesList(): MutableList<Shelf> {
        val shelves: MutableList<Shelf> = mutableListOf()
        for (i in 1..8) {
            shelves.add(Shelf(
                    id = i,
                    number = i.toString(),
                    status = createShelfStatus(),
                    goods = createGoodsList()))
        }

        return shelves
    }

    private fun createShelfStatus(): ShelfStatus {
        return if ((1..2).random() == 1) ShelfStatus.PROCESSED else ShelfStatus.DELETED
    }

    private fun createGoodsList(): MutableList<Good> {
        val goods: MutableList<Good> = mutableListOf()
        for (i in 1..100) {
            val facings = createFacings()

            goods.add(Good(
                    id = i,
                    sapCode = createSapCode(),
                    barCode = createBarCode(),
                    name = createGoodName(i),
                    sign = createGoodSign(facings),
                    totalFacings = facings))
        }

        return goods
    }

    private fun createFacings(): Int {
        return (0..35).random()
    }

    private fun createGoodSign(facings: Int): GoodSign {
        return if (facings == 0) GoodSign.MISSING else GoodSign.PRESENT
    }

    private fun createSapCode(): String {
        var sap = (1..999999).random().toString()
        while (sap.length < 6) {
            sap = "0$sap"
        }

        return sap
    }

    private fun createBarCode(): String {
        return (10000000000..99999999999).random().toString()
    }

    private fun createGoodName(index: Int): String {
        val goodNames = listOf(
                "Celebrated delight",
                "Celebrate",
                "Celebrated delightful an especial",
                "Am wound",
                "Fat new small",
                "As so seeing latter he sho",
                "Ecstatic elegance gay but disp",
                "Feel and make two real miss use",
                "Now summer who d",
                "Advantages entre",
                "Am wound worth water he linen a",
                "Sportsman do offending supporte",
                "Fat new smallness few s",
                "Is inquiry no he several excit",
                "Detrac",
                "Mirth learn it he",
                "Took sold add play may none him f",
                "Up hung mr",
                "Considered discov",
                "Secure shy favour length all tw",
                "Limits",
                "Draw from upon here gone a",
                "Called though excuse l",
                "Fat new smallness",
                "To th",
                "Whateve",
                "We leaf to snug on no need.",
                "To things so denied admire",
                "At principle perf",
                "At pr",
                "An con",
                "Mirth learn it he given. Is in",
                "Celebra",
                "Took s",
                "Sportsman do offend",
                "Strictly nu",
                "Mrs assured add private mar",
                "Up hung",
                "Made neat an",
                "If as increasing contrasted entrea",
                "Bed uncommonly his d",
                "At principle",
                "Mirth learn it",
                "Decisively advantages nor express",
                "To sure ca",
                "If in so b",
                "Took sold add play",
                "Their saved linen downs te",
                "To sure calm m",
                "We leaf to snu",
                "Secure shy favour length a",
                "Sitting hearted",
                "Indulgence contra",
                "Mirth lea",
                "Fortune da",
                "Effect if in",
                "Draw fond rank form nor the d",
                "Bed unc",
                "Mrs assured add priva",
                "He felicity no an at packages an",
                "We me rent been",
                "Up hung",
                "So by colonel h",
                "Happiness remainder joy but earnes",
                "Celebrated delightful an e",
                "Pain son rose more pa",
                "Made neat an",
                "Advantages entreatie",
                "Sentiments two occas",
                "So by colonel hearted fe",
                "Mrs assured add",
                "Any delicate you how kindn",
                "We leaf to snug on no need. Am",
                "Secure shy favour leng",
                "Uncommon",
                "Considered discovered ye",
                "Feel and mak",
                "How one dull get busy d",
                "Course sir people worthy horse",
                "Now summer who day looked o",
                "Detract yet de",
                "Draw fr",
                "Feel and make two r",
                "Way own uncommonly travel",
                "Any delicate you how kindness hor",
                "Ecstatic elegance",
                "Am wound worth w",
                "Effect if in up no depend seemed.",
                "Course sir pe",
                "Advanta",
                "Girl quit if",
                "Painful so he an comfort is ma",
                "Happiness rem",
                "As mr started",
                "Ecstatic el",
                "Estate was tended",
                "Mrs assu",
                "Mirth learn it he",
                "Any delicate you",
                "At principle perfe")

        return goodNames[index]
    }
}