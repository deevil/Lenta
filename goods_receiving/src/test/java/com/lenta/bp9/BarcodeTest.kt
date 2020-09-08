package com.lenta.bp9

import com.lenta.shared.utilities.gs1.EAN128Parser
import com.lenta.shared.utilities.gs1.ApplicationIdentifier
import com.lenta.shared.utilities.gs1.ElementStrings
import org.junit.Test

class BarcodeTest {

    @Test
    fun barcode() {
        val barcode = "010245675434568910500087654345|8008200723164070032007251640"
        val barcode1 = "0104607089699888111912113050"
        val barcode2 = "0102911423000011111912263100.01"
        val barcode3 = "01028773110000541050000115058070032020081020"
        val barcode4 = "(01)02877311000054(10)500001150580(8008)202008101515"
        val barcode5 = "01046070896998883050" // 01-баркод 30 - количество штучное (50 ШТ)
        val barcode6 = "010460708969988811191210" // 01-баркод 11 - дата производства yyMMdd (Верная )
        val barcode7 = "010460708969988811191211" // 01-баркод 11 - дата производства yyMMdd (Не верная )
        val barcode8 = "0104607089699888111912103050" // 01-баркод 11 - дата производства yyMMdd (Верная ) 30 - количество штучное (50 ШТ)
        val barcode9 = "0104607089699888111912113050" // 01-баркод 11 - дата производства yyMMdd (Не верная ) 30 - количество штучное (50 ШТ)
        val barcode10 = "0102911423000011111912263100.01" // 01-баркод 11 - дата производства yyMMdd (Верная ) 310 вес нетто, килограммы (0.01 КГ)
        val barcode11 = "01029114230000113100.01" // 01-баркод 310 вес нетто, килограммы (0.01 КГ)
        val barcode12 = "0103612345678904|11990102" // 01-баркод 310 вес нетто, килограммы (0.01 КГ)

        val gtinCodes = listOf(barcode)

        //listOf<String>(barcode, barcode1, barcode2, barcode3, barcode4)

//       EAN128Parser.parseWith(barcode4, EAN128Parser.EAN_01)
//       EAN128Parser.parseBy(barcode)

        gtinCodes.forEach { code ->

            println("------> Start Process code $code <-------")

            val result = ElementStrings.parse(code)
            val error = result.errorMessage // in case of a partial parse results this describes the error encountered
            val isPartial = result.isPartial

            println("--> isEmpty = ${result.isEmpty} ")
            println("--> error = $error ")
            println("--> isPartial = $isPartial ")

            val gtin = result.getString(ApplicationIdentifier.GTIN)
            val batch = result.getString(ApplicationIdentifier.BATCH_OR_LOT_NUMBER)
            val weight = result.getString(ApplicationIdentifier.ITEM_NET_WEIGHT_KG)
            val count = result.getString(ApplicationIdentifier.VARIABLE_COUNT)
            val dateProduction = result.getDate(ApplicationIdentifier.PRODUCTION_DATE)
            val dateTimeProduction = result.getDate(ApplicationIdentifier.PRODUCTION_DATE_AND_TIME)
            val dateTimeExpiration = result.getDate(ApplicationIdentifier.EXPIRATION_DATE_AND_TIME)

            println("--> gtin = $gtin ")
            println("--> batch = $batch ")
            println("--> weight = $weight ")
            println("--> count = $count ")
            println("---> dateProduction = $dateProduction ")
            println("---> dateTimeProduction = $dateTimeProduction ")
            println("---> dateTimeExpiration = $dateTimeExpiration ")

            if (error != null && error.isNotEmpty()) {
                var gsResult = EAN128Parser.parseBy(code)
                if (gsResult.isEmpty()) {
                    gsResult = EAN128Parser.parse(code, false)
                }
                val gtin2 = gsResult.entries.find { it.key.AI == ApplicationIdentifier.GTIN.key }?.value.orEmpty()
                println("--> gtin2 = $gtin2 ")

            } else {

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