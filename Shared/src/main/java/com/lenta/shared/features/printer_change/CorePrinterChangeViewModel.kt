package com.lenta.shared.features.printer_change

import com.lenta.shared.platform.viewmodel.CoreViewModel

abstract class CorePrinterChangeViewModel : CoreViewModel() {
    abstract fun onClickBack()
    abstract fun onClickApp()
    abstract fun onBackPressed()
}
