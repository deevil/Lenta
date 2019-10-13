package com.lenta.shared.print

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either

class DatamaxNetPrinter(override val ip: String) : INetPrinter() {

    override val port: Int = 515

    override val printerType: NetPrinterType
        get() = NetPrinterType.Datamax

    override fun calibrate(): Either<Failure, Boolean> {
        return Either.Right(true)
    }

}