package com.lenta.bp9

import com.lenta.shared.utilities.EAN128Parser
import org.junit.Test

class BarcodeTest {

    @Test
    fun barcode() {
        val barcode = "0102877311000054105000011505808008202008101515"
        val barcode1 = "0104607089699888111912113050"
        val barcode2 = "0102911423000011111912263100.01"
        val barcode3 = "01028773110000541050000115058070032020081020"
        val barcode4 = "(01)02877311000054(10)500001150580(8008)202008101515"

//       EAN128Parser.parseWith(barcode4, EAN128Parser.EAN_01)
//       EAN128Parser.parseBy(barcode)

        val results = EAN128Parser.parseWith(barcode)
        println(results.toString())

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