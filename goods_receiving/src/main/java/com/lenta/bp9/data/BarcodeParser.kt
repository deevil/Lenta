package com.lenta.bp9.data

import com.lenta.shared.models.core.BarcodeData
import com.lenta.shared.platform.viewmodel.BarcodeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BarcodeParser : BarcodeViewModel() {
    suspend fun getBarcodeData(data: String): BarcodeData =
            withContext(context = Dispatchers.IO) {
                processBarcode(data)
            }
}