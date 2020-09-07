package com.lenta.bp9

import com.lenta.shared.utilities.gs1.*
import org.junit.Test

class BarcodePatternTest {

    @Test
    fun barcodePattern() {
        var groutSeperator = 29.toChar()
        val barcode4 = "010245675434568910500087654345${groutSeperator}80082007231640${groutSeperator}70032007251640${groutSeperator}310001"
        val weightBarcode = "(01)02911423000011(310)0.01"
        var gsResult = EAN128Parser.parse(barcode4, false)

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