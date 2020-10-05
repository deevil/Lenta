package com.lenta.shared.features.printer_change

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.fast.ZmpUtz26V001
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class PrinterChangeViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: ICoreNavigator

    @Inject
    lateinit var printerManager: PrinterManager

    private val printers: MutableLiveData<List<PrinterUi>> = MutableLiveData()
    val printersNames: MutableLiveData<List<String>> = printers.map { printers ->
        printers?.map { "${it.number}-${it.printerName}" }
    }
    val selectedPosition: MutableLiveData<Int> = MutableLiveData()
    val selectedDescription: MutableLiveData<String> = selectedPosition.map {
        it?.let { position ->
            printers.value?.getOrNull(position)?.printerInfo
        }
    }

    private val txtNotFoundPrinter: MutableLiveData<String> = MutableLiveData()

    fun setTxtNotFoundPrinter(string: String) {
        this.txtNotFoundPrinter.value = string
    }

    init {
        launchUITryCatch {
            handlePrinters(printerManager.getAllPrinters())
        }
    }

    private fun handlePrinters(list: List<ZmpUtz26V001.ItemLocal_ET_PRINTERS>) {
        launchUITryCatch {
            printers.value = list.mapIndexed { index, printerInfo ->
                PrinterUi(
                        number = "${index + 1}",
                        printerName = printerInfo.printerName.orEmpty(),
                        printerInfo = printerInfo.printerInfo.orEmpty()
                )
            }

            val currentPrinter = printerManager.getCurrentPrinter()

            if (list.isNotEmpty() && selectedPosition.value == null) {
                var pos = 0
                for ((i, item) in list.withIndex()) {
                    if (item.printerName == currentPrinter?.printerName) {
                        pos = i
                        break
                    }
                }
                onClickPosition(pos)
            } else {
                screenNavigator.goBack()
                screenNavigator.openAlertScreen(message = txtNotFoundPrinter.value!!, pageNumber = "95")
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }


    fun onClickApply() {
        launchUITryCatch {
            printers.value?.getOrNull(selectedPosition.value!!).let {
                printerManager.setPrinter(it?.number?.toIntOrNull())
                screenNavigator.goBack()
            }

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
