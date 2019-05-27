package com.lenta.shared.features.printer_change

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.fmp.resources.ZmpUtz26V001
import com.lenta.shared.requests.db.PrinterChangeDBRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
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

    private val printers: MutableLiveData<List<PrinterUi>> = MutableLiveData()
    val printersNames: MutableLiveData<List<String>> = printers.map { printers ->
        printers?.map { it.printerName }
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
            printerChangeDBRequest(sessionInfo.market!!).either(::handleFailure, ::handlePermissions)
        }
    }

    private fun handlePermissions(list: List<ZmpUtz26V001.ItemLocal_ET_PRINTERS>) {
        printers.value = list.map { PrinterUi(number = it.werks, printerName = it.printername, printerInfo = it.printerinfo) }
        if (list.isNotEmpty() && selectedPosition.value == null) {
            onClickPosition(0)
        } else {
            screenNavigator.openAlertScreen(txtNotFoundPrinter.value!!)
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }


    fun onClickApp() {
        sessionInfo.printer = printers.value?.getOrNull(selectedPosition.value!!)?.printerName
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
