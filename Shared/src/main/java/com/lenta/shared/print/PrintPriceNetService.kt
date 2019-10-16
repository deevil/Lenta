package com.lenta.shared.print

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.flatMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException
import javax.inject.Inject

class PrintPriceNetService @Inject constructor(private val priceTagGenerator: IPriceTagGenerator) : IPrintPriceNetService {

    override suspend fun print(ip: String, printTemplate: PrintTemplate, printPriceInfo: PrintPriceInfo): Either<Failure, Boolean> {
        val printer = getPrinter(printTemplate.printerType, ip)
        return withContext(Dispatchers.IO) {
            return@withContext priceTagGenerator.generatePriceTagForPrint(printTemplate, printPriceInfo).flatMap {
                printer.print(it)
            }
        }

    }

    override suspend fun calibrate(ip: String, printTemplate: PrintTemplate): Either<Failure, Boolean> {
        val printer = getPrinter(printTemplate.printerType, ip)
        return withContext(Dispatchers.IO) {
            printer.calibrate()
        }
    }

    private fun getPrinter(printerType: NetPrinterType, ip: String): INetPrinter {
        return when (printerType) {
            NetPrinterType.Zebra -> ZebraNetPrinter(ip)
            NetPrinterType.Datamax -> DatamaxNetPrinter(ip)
            else -> throw IllegalArgumentException("printerType: $printerType is not supported")
        }

    }

}

interface IPrintPriceNetService {
    suspend fun print(ip: String, printTemplate: PrintTemplate, printPriceInfo: PrintPriceInfo): Either<Failure, Boolean>
    suspend fun calibrate(ip: String, printTemplate: PrintTemplate): Either<Failure, Boolean>
}

