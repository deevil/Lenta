package com.lenta.shared.print

class ZebraNetPrinter(override val ip: String) : INetPrinter() {

    //val test = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ".toByteArray()

    override val printerType: PrinterType
        get() = PrinterType.Zebra

    override val port: Int = 6101


}