package com.lenta.bp10.features.printer_change

import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.features.printer_change.CorePrinterChangeViewModel
import javax.inject.Inject

class PrinterChangeViewModel : CorePrinterChangeViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    override fun onClickBack() {
        screenNavigator.goBack()
    }

    override fun onClickApp() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        screenNavigator.openAlertScreen("onClickApp")
    }

    override fun onBackPressed() {
    }

}
