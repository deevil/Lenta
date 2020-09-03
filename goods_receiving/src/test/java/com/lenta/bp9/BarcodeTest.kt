package com.lenta.bp9

import com.lenta.shared.utilities.EAN128Parser
import com.lenta.shared.utilities.gs1.ApplicationIdentifier
import com.lenta.shared.utilities.gs1.ElementStrings
import org.junit.Test

class BarcodeTest {

    @Test
    fun barcode() {
        val barcode = "0102877311000054105000011505808008202008101515"
        val barcode1 = "0104607089699888111912113050"
        val barcode2 = "0102911423000011111912263100.01"
        val barcode3 = "01028773110000541050000115058070032020081020"
        val barcode4 = "(01)02877311000054(10)500001150580(8008)202008101515"

        val gtinCodes = listOf<String>(barcode, barcode1, barcode2, barcode3, barcode4)

//       EAN128Parser.parseWith(barcode4, EAN128Parser.EAN_01)
//       EAN128Parser.parseBy(barcode)

        gtinCodes.forEach { code ->

            println("------> Start Process code $code <-------")

            val result = ElementStrings.parse(code)
            val error = result.errorMessage // in case of a partial parse results this describes the error encountered
            val isPartial = result.isPartial

            println("--> isEmpty = ${result.isEmpty} ")
            println("--> error = $error ")
            println("--> isPartial isEmpty = $isPartial ")

            if (error.isNullOrEmpty()) {
                val gtin = result.getString(ApplicationIdentifier.GTIN)
                val batch = result.getString(ApplicationIdentifier.BATCH_OR_LOT_NUMBER)
                val weight = result.getDecimal(ApplicationIdentifier.ITEM_NET_WEIGHT_KG)
                val date = result.getDate(ApplicationIdentifier.BEST_BEFORE_DATE)

                println("--> gtin = $gtin ")
                println("--> batch = $batch ")
                println("--> weight = $weight ")
                println("---> date = $date ")
            } else {
                val eanResult = EAN128Parser.parse(code, false)
                val gtin = eanResult.keys.find { it.AI == EAN128Parser.EAN_01 }
                println("--> gtin = $gtin ")
            }
            println("------> END <------ ")
        }
//
//        val eanCode = results.entries.find { pair ->
//            pair.key.AI == EAN128Parser.EAN_01
//        }?.value
//        println(eanCode)
//
//        val eanBanch = results.entries.find { pair ->
//            pair.key.AI == "10"
//        }?.value
//        println(eanBanch)
//
//        val eanData = results.entries.find { pair ->
//            pair.key.AI == "8008"
//        }?.value
//        println(eanData)
    }
}