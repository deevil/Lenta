package com.lenta.bp9

import com.lenta.shared.utilities.gs1.*
import org.junit.Test

class BarcodePatternTest {

    @Test
    fun barcodePattern() {
        val barcode4 = "(01)02877311000054(10)500001150580(8008)202008101515"
        var gsResult = EAN128Parser.parseBy(barcode4)

        val gtin = gsResult.getString(ApplicationIdentifier.GTIN)
        val batch = gsResult.getString(ApplicationIdentifier.BATCH_OR_LOT_NUMBER)
        val weight = gsResult.getDouble(ApplicationIdentifier.ITEM_NET_WEIGHT_KG)
        val count = gsResult.getDouble(ApplicationIdentifier.COUNT_OF_TRADE_ITEMS)
        val dateProduction = gsResult.getDate(ApplicationIdentifier.PRODUCTION_DATE_AND_TIME)
        val dateExpiration = gsResult.getDate(ApplicationIdentifier.EXPIRATION_DATE_AND_TIME)

        println("--> gtin = $gtin ")
        println("--> batch = $batch ")
        println("--> weight = $weight ")
        println("--> count = $count ")
        println("---> dateProduction = $dateProduction ")
        println("---> dateExpiration = $dateExpiration ")

    }
}