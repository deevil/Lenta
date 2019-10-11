package com.lenta.shared.print

class DatamaxNetPrinter(override val ip: String) : INetPrinter() {
    override val port: Int = 515

    override val printerType: PrinterType
        get() = PrinterType.Datamax

    override fun convertStringToBytes(data: String): ByteArray {
        return super.convertStringToBytes(data)
    }
}