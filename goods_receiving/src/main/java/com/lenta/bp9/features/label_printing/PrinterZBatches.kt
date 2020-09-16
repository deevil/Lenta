package com.lenta.bp9.features.label_printing

import android.content.Context
import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.utilities.Logg
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class LabelPrintingZBatches @Inject constructor(
        private val context: Context,
        private val gson: Gson,
        private val analyticsHelper: AnalyticsHelper
) : IPrinterZBatches {

    override fun printLabel(labelInfo: LabelZBatchesInfo, ip: String): Either<Failure, Boolean> {
        val template = readTemplateFromAsset() ?: return Either.Left(Failure.FileReadingError)

        generatePriceLabelFromTemplate(labelInfo, template).let {
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

        // Добавляем Маркер UTF-8, если его нет
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

    private fun generatePriceLabelFromTemplate(labelInfo: LabelZBatchesInfo, template: String): String? {
        if (template.isBlank()) {
            return null
        }

        var res = template

        mutableMapOf<String, String>().apply {
            put("GOODSNAME", labelInfo.goodsName)
            put("GOODSCODE", labelInfo.goodsName)
            put("DATEEXPIR", labelInfo.shelfLife)
            put("PRODUCTTIME", labelInfo.productTime)
            put("DELIVERY", labelInfo.delivery)
            put("PROVIDER", labelInfo.provider)
            put("ZBATCH", labelInfo.batchNumber)
            put("PRODUCER", labelInfo.manufacturer)
            put("WEIGHER", labelInfo.weigher)
            put("QIANTITY", labelInfo.quantity)
            put("BARCODE", labelInfo.barcode)
            put("TEXTBARCODE", labelInfo.barcodeText)
            put("COPIES", labelInfo.copies)
        }.forEach {
            res = res.replace("@${it.key}", it.value)
        }

        return res
    }

    private fun readTemplateFromAsset(): String? {
        val sbTemplate = StringBuilder()

        return try {
            val inputStream = context.assets.open("print_templates/inner_grz_zbatches_tag.prn")
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

interface IPrinterZBatches {
    fun printLabel(labelInfo: LabelZBatchesInfo, ip: String): Either<Failure, Boolean>
}

//https://trello.com/c/MoGz5T0x
data class LabelZBatchesInfo(
        /** Наименование продукта */
        val goodsName: String,
        /** sap-код товара */
        val goodsCode: String,
        /** срок годности, брать из ФМ GRZ_45 */
        val shelfLife: String,
        /** дата, которую ввел пользователь при приемке */
        val productTime: String,
        /** поставка, ФМ GRZ_15, структура ES_TASK~VBELN **/
        val delivery: String,
        /** поставщик **/
        val provider: String,
        /** номер Z-партии, брать из ФМ GZR_45, ET_ZPARTS_DIFF~BATCH **/
        val batchNumber: String,
        /** производитель **/
        val manufacturer: String,
        /** Табельный номер */
        val weigher: String,
        /** Количество **/
        val quantity: String,
        /** Штрихкод */
        val barcode: String,
        /** Штрихкод для отображения в текстовом виде */
        val barcodeText: String,
        /** Кол-во копий */
        val copies: String
)