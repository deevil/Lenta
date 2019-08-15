package com.lenta.shared.features.printer_change

import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz26V001
import com.lenta.shared.settings.IAppSettings
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PrinterManager @Inject constructor(
        private val hyperHive: HyperHive,
        private val sessionInfo: ISessionInfo,
        private val appSettings: IAppSettings
) {

    suspend fun getCurrentPrinter(): ZmpUtz26V001.ItemLocal_ET_PRINTERS? {
        return getAllPrinters(sessionInfo.market).firstOrNull { sessionInfo.printer == it.printerName }
    }


    suspend fun getAllPrinters(tkNumber: String? = sessionInfo.market): List<ZmpUtz26V001.ItemLocal_ET_PRINTERS> {
        if (tkNumber == null) {
            return emptyList()
        }
        return withContext(Dispatchers.IO) {
            @Suppress("INACCESSIBLE_TYPE")
            return@withContext ZmpUtz26V001(hyperHive).localHelper_ET_PRINTERS.getWhere("WERKS = \"$tkNumber\"")
        }
    }

    suspend fun setDefaultPrinterForTk(tkNumber: String? = sessionInfo.market) {
        setPrinter(1, tkNumber = tkNumber)
    }

    suspend fun setPrinter(printerNumber: Int?, tkNumber: String? = sessionInfo.market) {

        val index = (printerNumber ?: 0) - 1

        getAllPrinters(tkNumber).getOrNull(index)?.let { printer ->
            saveToSettings(printer.printerName, printerNumber)
            return
        }

        saveToSettings(null, null)

    }

    private fun saveToSettings(printerName: String?, printerNumber: Int?) {
        sessionInfo.printer = printerName
        sessionInfo.printerNumber = printerNumber?.toString()
        appSettings.printer = sessionInfo.printer
        appSettings.printerNumber = sessionInfo.printerNumber
    }

}


