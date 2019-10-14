package com.lenta.shared.print

import androidx.annotation.WorkerThread
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket

abstract class INetPrinter {

    abstract val ip: String
    abstract val port: Int
    abstract val printerType: NetPrinterType

    @WorkerThread
    fun print(data: String): Either<Failure, Boolean> {
        val bytes = convertStringToBytes(data)
        var socket: Socket? = null
        try {
            socket = Socket()
            socket.connect(InetSocketAddress(ip, port), 3000)
            socket.getOutputStream().write(bytes)
            socket.close()
        } catch (e: Exception) {
            return Either.Left(Failure.NetworkConnection)
        } finally {
            socket?.close()
        }
        return Either.Right(true)
    }

    abstract fun calibrate(): Either<Failure, Boolean>


    open fun convertStringToBytes(data: String): ByteArray {
        return data.toByteArray()
    }


}