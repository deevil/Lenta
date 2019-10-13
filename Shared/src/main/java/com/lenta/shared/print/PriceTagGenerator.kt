package com.lenta.shared.print

import android.content.Context
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.utilities.extentions.divideRoubleWithKop
import com.lenta.shared.utilities.extentions.getFormattedTimeForPriceTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.util.*
import javax.inject.Inject


class PriceTagGenerator @Inject constructor(private val context: Context) : IPriceTagGenerator {

    companion object {
        const val LENGTH_MAX_ADDRESS = 76
    }

    override suspend fun generatePriceTagForPrint(printTemplate: PrintTemplate, printPriceInfo: PrintPriceInfo): Either<Failure, String> {
        return withContext(Dispatchers.IO) {

            val template = readTemplateFromAsset(printTemplate)
                    ?: return@withContext Either.Left(Failure.FileReadingError)

            generatePriceTagFromTemplate(printTemplate, printPriceInfo, template).let {
                if (it == null) {
                    return@withContext Either.Left(Failure.PrintTemplateError)
                } else {
                    return@withContext Either.Right(it)
                }
            }


        }
    }

    private fun generatePriceTagFromTemplate(printTemplate: PrintTemplate, printPriceInfo: PrintPriceInfo, template: String): String? {

        return when (printTemplate.printerType) {
            NetPrinterType.Zebra -> generatePricePrintStringForZebra(printPriceInfo, template)
            else -> null
        }

    }

    private fun generatePricePrintStringForZebra(printPriceInfo: PrintPriceInfo, template: String): String? {
        if (template.isBlank()) {
            return null
        }

        var res = template

        mutableMapOf<String, String>().apply {
            put("GOODSNAME",
                    printPriceInfo.goodsName
                            .replace("ё", "е")
                            .replace("Ё", "Е")
                            .take(60)
            )
            put("ADDRESS",
                    printPriceInfo.address.let {
                        if (it.length > LENGTH_MAX_ADDRESS) {
                            "${it.take(LENGTH_MAX_ADDRESS)} ..."
                        } else {
                            it
                        }
                    }
            )

            printPriceInfo.price1.let { price ->
                price.divideRoubleWithKop().let {
                    put("RUB1", it.first)
                    put("KOP1", it.second)
                }
            }

            printPriceInfo.price2.let { price ->
                price.divideRoubleWithKop().let {
                    put("RUB2", it.first)
                    put("KOP2", it.second)
                }
            }

            put("GOODSCODE", printPriceInfo.productNumber)
            put("BARCODE", printPriceInfo.ean)
            put("DATETIME", printPriceInfo.date.getFormattedTimeForPriceTag())
            put("COPIES", printPriceInfo.copies.toString())
            put("PROMOBEGIN", printPriceInfo.promoBegin)
            put("PROMOEND", printPriceInfo.promoEnd)
        }.forEach {
            res = res.replace("@${it.key}", it.value)
        }
        return res
    }


    private fun readTemplateFromAsset(printTemplate: PrintTemplate): String? {
        val sbTemplate = StringBuilder()
        return try {
            val inputStream = context.assets.open(printTemplate.templatePath)
            val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
            var s: String?
            var prefix = ""
            while (reader.readLine().apply { s = this } != null) {
                sbTemplate.append(prefix)
                sbTemplate.append(s)
                prefix = "\n"
            }
            reader.close()
            sbTemplate.toString()
        } catch (e: Exception) {
            null
        }
    }


}

data class PrintPriceInfo(
        val goodsName: String,
        val price1: Double,
        val price2: Double,
        val productNumber: String,
        val ean: String,
        val date: Date,
        val address: String,
        val promoBegin: String,
        val promoEnd: String,
        val copies: Int
)


interface IPriceTagGenerator {
    suspend fun generatePriceTagForPrint(printTemplate: PrintTemplate, printPriceInfo: PrintPriceInfo): Either<Failure, String>
}

enum class PrintTemplate(
        val printerType: NetPrinterType,
        val templatePath: String
) {
    Zebra_Red_6_6(NetPrinterType.Zebra, "print_templates/Red6x6_Zebra.zpl"),
    Zebra_Yellow_6_6(NetPrinterType.Zebra, "print_templates/Yellow6x6_Zebra.zpl"),
    Datamax_Red_6_6(NetPrinterType.Datamax, "print_templates/Red6x6_Datamax.trf"),
    Datamax_Yellow_6_6(NetPrinterType.Datamax, "print_templates/Yellow6x6_Datamax.trf")
}

