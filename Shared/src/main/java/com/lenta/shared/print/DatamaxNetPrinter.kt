package com.lenta.shared.print

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.google.common.base.Charsets.US_ASCII


class DatamaxNetPrinter(override val ip: String) : INetPrinter() {

    override val port: Int = 515

    private val rusAlphabet = listOf(
            'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я',
            'а', 'б', 'в', 'г', 'д', 'е', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я'
    )

    val magicByte = 63.toByte()

    override val printerType: NetPrinterType
        get() = NetPrinterType.Datamax

    override fun calibrate(): Either<Failure, Boolean> {
        return Either.Right(true)
    }

    override fun convertStringToBytes(data: String): ByteArray {

        val result = ByteArray(data.length)
        data.forEachIndexed { index, c ->
            var b = c.toString().toByteArray(US_ASCII)[0]
            if (b == magicByte) {
                b = getByteFromRusChar(c)
            }
            result[index] = b
        }

        //return data.toByteArray(Charset.forName("windows-1251"))
        return result

    }

    private fun getByteFromRusChar(char: Char): Byte {
        val rusCharIndex = rusAlphabet.indexOf(char)
        if (rusCharIndex >= 0) {
            return (rusCharIndex + 176).toByte()
        }
        return magicByte
    }

}