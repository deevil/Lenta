package com.lenta.shared.features.printer_change

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz26V001
import com.lenta.shared.requests.db.PrinterChangeDBRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class PrinterChangeViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: ICoreNavigator

    @Inject
    lateinit var printerChangeDBRequest: PrinterChangeDBRequest

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var appSettings: IAppSettings

    private val printers: MutableLiveData<List<PrinterUi>> = MutableLiveData()
    val printersNames: MutableLiveData<List<String>> = printers.map { printers ->
        printers?.mapIndexed { index, printerUi -> "${printerUi.number}-${printerUi.printerName}" }
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
        viewModelScope.launch {
            printerChangeDBRequest(sessionInfo.market!!).either(::handleFailure, ::handlePrinters)
        }
    }

    private fun handlePrinters(list: List<ZmpUtz26V001.ItemLocal_ET_PRINTERS>) {
        printers.value = list.mapIndexed { index, printerInfo ->
            PrinterUi(
                    number = "${index + 1}",
                    printerName = printerInfo.printername,
                    printerInfo = printerInfo.printerinfo
            )
        }
        if (list.isNotEmpty() && selectedPosition.value == null) {
            var pos = 0
            for ((i, item) in list.withIndex()) {
                if (item.printername == appSettings.printer) {
                    pos = i
                    break
                }
            }
            onClickPosition(pos)
        } else {
            screenNavigator.goBack()
            screenNavigator.openAlertScreen(message = txtNotFoundPrinter.value!!, pageNumber = "10/95")
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }


    fun onClickApply() {
        printers.value?.getOrNull(selectedPosition.value!!).let {
            sessionInfo.printer = it?.printerName
            sessionInfo.printerNumber = it?.number
        }

        appSettings.printer = sessionInfo.printer
        appSettings.printerNumber = sessionInfo.printerNumber
        screenNavigator.goBack()
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
