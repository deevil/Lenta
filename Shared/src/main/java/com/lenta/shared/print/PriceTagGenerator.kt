package com.lenta.shared.print

import android.content.Context
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.lang.Exception
import java.nio.charset.StandardCharsets
import javax.inject.Inject


class PriceTagGenerator @Inject constructor(private val context: Context) : IPriceTagGenerator {

    override suspend fun generatePriceTag(priceTemplate: PriceTemplate): Either<Failure, String> {
        return withContext(Dispatchers.IO) {

            val template = readTemplateFromAsset(priceTemplate)

            if (template == null) {
                return@withContext Either.Left(Failure.FileReadingError)
            }

            return@withContext Either.Right(template)
        }
    }

    private fun readTemplateFromAsset(priceTemplate: PriceTemplate): String? {
        val sbTemplate = StringBuilder()
        return try {
            val inputStream = context.assets.open(priceTemplate.templatePath)
            val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8) as Reader?)
            var s: String?
            while (reader.readLine().apply { s = this } != null) {
                sbTemplate.append(s)
            }
            reader.close()
            sbTemplate.toString()
        } catch (e: Exception) {
            null
        }
    }


}

data class PriceTagInfo(
        val goodsName1: String,
        val goodsName2: String,
        val goodsName3: String
)


interface IPriceTagGenerator {
    suspend fun generatePriceTag(priceTemplate: PriceTemplate): Either<Failure, String>
}

enum class PriceTemplate(
        val printerType: PrinterType,
        val templatePath: String
) {
    Zebra_Red_6_6(PrinterType.Zebra, "print_templates/Red6x6_Zebra.zpl"),
    Zebra_Yellow_6_6(PrinterType.Zebra, "print_templates/Yellow6x6_Zebra.zpl"),
    Datamax_Red_6_6(PrinterType.Zebra, "print_templates/Red6x6_Datamax.trf"),
    Datamax_Yellow_6_6(PrinterType.Zebra, "print_templates/Yellow6x6_Datamax.trf")
}

