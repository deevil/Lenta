package com.lenta.bp16.data

import android.content.Context
import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.utilities.Logg
import com.lenta.shared.settings.IAppSettings
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class Printer @Inject constructor(
        private val context: Context,
        private val sessionInfo: ISessionInfo,
        private val appSettings: IAppSettings,
        private val gson: Gson,
        private val analyticsHelper: AnalyticsHelper
) : IPrinter {

    override fun printTag(printInnerTagInfo: PrintInnerTagInfo): Either<Failure, Boolean> {
        val ip = appSettings.printerIpAddress ?: ""

        val template = readTemplateFromAsset() ?: return Either.Left(Failure.FileReadingError)

        generatePriceTagFromTemplate(printInnerTagInfo, template).let {
            return if (it == null) {
                Either.Left(Failure.PrintTemplateError)
            } else {
                printText(it, ip)
            }
        }
    }


    @WorkerThread
    private fun printText(data: String, ip: String): Either<Failure, Boolean> {
        val bytes = convertStringToBytes(data)
        var socket: Socket? = null
        try {
            socket = Socket()
            socket.connect(InetSocketAddress(ip, 6101), 3000)
            socket.getOutputStream().write(bytes)
            socket.close()

        } catch (e: Exception) {
            Logg.e { "print exception: $e" }
            return Either.Left(Failure.NetworkConnection)
        } finally {
            socket?.close()
        }
        return Either.Right(true)
    }

    private fun convertStringToBytes(data: String): ByteArray {

        val byteArray = data.toByteArray()

        val byte0 = 0xEF.toByte()
        val byte1 = 0xBB.toByte()
        val byte2 = 0xBF.toByte()

        //добавляем Маркер UTF-8, если его нет
        return (if (byteArray[0] != byte0 && byteArray[1] != byte1 && byteArray[2] != byte2) {
            ByteArray(3 + byteArray.size).apply {
                set(0, byte0)
                set(1, byte1)
                set(2, byte2)
                byteArray.forEachIndexed { index, byte ->
                    set(index + 3, byte)
                }
            }
        } else {
            byteArray
        })
    }

    private fun generatePriceTagFromTemplate(printInnerTagInfo: PrintInnerTagInfo, template: String): String? {

        if (template.isBlank()) {
            return null
        }

        var res = template

        mutableMapOf<String, String>().apply {

            put("QUANTITY", printInnerTagInfo.quantity)
            put("CODECONT", printInnerTagInfo.codeCont)
            put("STORCOND", printInnerTagInfo.storCond)
            put("PLANAUFFINISH", printInnerTagInfo.planAufFinish)
            put("AUFNR", printInnerTagInfo.aufnr)
            put("NAMEOSN", printInnerTagInfo.nameOsn)
            put("DATEEXPIR", printInnerTagInfo.dateExpir)
            put("GOODSNAME", printInnerTagInfo.goodsName)
            put("WEIGHER", printInnerTagInfo.weigher)
            put("PRODUCTTIME", printInnerTagInfo.productTime)
            put("NAMEDONE", printInnerTagInfo.nameDone)
            put("GOODSCODE", printInnerTagInfo.goodsCode)
            put("BARCODE", printInnerTagInfo.barcode)


            /*put("GOODSNAME",
                    printInnerTagInfo.goodsName
                            .replace("ё", "е")
                            .replace("Ё", "Е")
            )
            put("ADDRESS",
                    printPriceInfo.address.let {
                        if (it.length > PriceTagGenerator.LENGTH_MAX_ADDRESS) {
                            "${it.take(PriceTagGenerator.LENGTH_MAX_ADDRESS)} ..."
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
            put("PROMOEND", printPriceInfo.promoEnd)*/
        }.forEach {
            res = res.replace("@${it.key}", it.value)
        }
        return res


    }

    private fun readTemplateFromAsset(): String? {
        val sbTemplate = StringBuilder()
        return try {
            val inputStream = context.assets.open("print_templates/inner_pro_tag.prn")
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

interface IPrinter {
    fun printTag(printInnerTagInfo: PrintInnerTagInfo): Either<Failure, Boolean>
}

data class PrintInnerTagInfo(
        /**
         * количество
         **/
        val quantity: String,
        /**
         * номер тары
         **/
        val codeCont: String,
        /**
         * условия хранения
         */
        val storCond: String,
        /**
         * Плановое окончание тех. процесса
         */
        val planAufFinish: String,
        /**
         * Номер технологического заказа
         */
        val aufnr: String,
        /**
         * Наименование следующего передела
         */
        val nameOsn: String,
        /**
         * Годен до:
         */
        val dateExpir: String,
        /**
         * Наименование продукта
         */
        val goodsName: String,
        /**
         * номер весов
         */
        val weigher: String,
        /**
         * Изготовлено
         */
        val productTime: String,
        /**
         * Наименование готового продукта
         */
        val nameDone: String,
        /**
         * Код товара
         */
        val goodsCode: String,
        /**
         * barcode
         */
        val barcode: String
)