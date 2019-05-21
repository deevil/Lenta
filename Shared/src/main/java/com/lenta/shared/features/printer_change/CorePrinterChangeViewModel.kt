package com.lenta.shared.features.printer_change

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener

abstract class CorePrinterChangeViewModel : CoreViewModel(), OnPositionClickListener {
    abstract fun onClickBack()
    abstract fun onClickApp()
    abstract fun onBackPressed()

    val printers: MutableLiveData<List<PrinterUi>> = MutableLiveData()
    val printersNames: MutableLiveData<List<String>> = printers.map { printers ->
        printers?.map { it.printerName }
    }
    val selectedPosition: MutableLiveData<Int> = MutableLiveData()
    val selectedDescription: MutableLiveData<String> = selectedPosition.map {
        it?.let { position ->
            printers.value?.getOrNull(position)?.printerInfo
        }
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }
}

data class PrinterUi(
        val number: String,
        val printerName: String,
        val printerInfo: String

)
