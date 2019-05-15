package com.lenta.bp10.features.printer_change

import androidx.lifecycle.viewModelScope
import com.lenta.bp10.account.SessionInfo
import com.lenta.bp10.fmp.resources.fast.ZmpUtz26V001
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.db.PrinterChangeDBRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.printer_change.CorePrinterChangeViewModel
import com.lenta.shared.features.printer_change.PrinterUi
import kotlinx.coroutines.launch
import javax.inject.Inject

class PrinterChangeViewModel : CorePrinterChangeViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var printerChangeDBRequest: PrinterChangeDBRequest

    @Inject
    lateinit var sessionInfo: SessionInfo

    init {
        viewModelScope.launch {
            printerChangeDBRequest(sessionInfo.market!!).either(::handleFailure, ::handlePermissions)
        }
    }

    private fun handlePermissions(list: List<ZmpUtz26V001.ItemLocal_ET_PRINTERS>) {
        printers.value = list.map { PrinterUi(number = it.werks, printername = it.printername, printerinfo = it.printerinfo) }
        if (list.isNotEmpty() && selectedPosition.value == null) {
            onClickPosition(0)
        }
        else {
            screenNavigator.openAlertScreen("Принтеров для данного ТК не найдено")
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }

    override fun onClickBack() {
        screenNavigator.goBack()
    }

    override fun onClickApp() {
        sessionInfo.printer = printers.value?.getOrNull(selectedPosition.value!!)?.printername
        screenNavigator.openMainMenuScreen()
    }

    override fun onBackPressed() {
    }

}
