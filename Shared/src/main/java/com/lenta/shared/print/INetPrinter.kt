package com.lenta.shared.print

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Socket

abstract class INetPrinter {

    abstract val ip: String
    abstract val port: Int
    abstract val printerType: PrinterType

    private val CALIBRATE = "! U1 setvar \"media.type\" \"label\"\r\n! U1 setvar \"media.sense_mode\" \"bar\"\r\n~JC^XA^JUS^XZ\r\n! U1 do \"device.reset\" \"\"\r\n"

    suspend fun print(data: String) {
        withContext(Dispatchers.IO) {
            val bytes = convertStringToBytes(data)
            val socket = Socket(ip, port)
            socket.getOutputStream().write(bytes)
            socket.close()
        }
    }

    suspend fun calibrate() {
        print(CALIBRATE)
    }


    open fun convertStringToBytes(data: String): ByteArray {
        return data.toByteArray()
    }


}