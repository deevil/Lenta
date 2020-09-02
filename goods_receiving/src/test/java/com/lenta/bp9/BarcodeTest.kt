package com.lenta.bp9

import com.lenta.shared.utilities.EAN128Parser
import org.junit.Test


class BarcodeTest {
    @Test
    fun parseBarcode() {

        val barcode = "(01)02877311000054(10)500001150580(8008)202008101515"
        val barcode1 = "012877311000054105000011505808008202008101515"
        val barcode2 = "0102877311000054(050000115058070032020081015"
        val barcode3 = "0102911423000011111912263100.01"
        val barcode4 = "0104607089699888111912113050"

        println(EAN128Parser.parse(barcode1, false))
    }
}