package com.lenta.shared.print

class ZebraNetPrinter(override val ip: String) : INetPrinter() {

    //val test = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ".toByteArray()

    override val printerType: PrinterType
        get() = PrinterType.Zebra

    override val port: Int = 6101

    override fun convertStringToBytes(data: String): ByteArray {

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


}