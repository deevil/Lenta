package com.lenta.bp10.features.printer_change

import com.lenta.bp10.R
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.features.printer_change.CorePrinterChangeFragment
import com.lenta.shared.utilities.extentions.provideViewModel

class PrinterChangeFragment : CorePrinterChangeFragment() {

    override fun getPageNumber(): String = "10/53"

    override fun getViewModel(): PrinterChangeViewModel {
        provideViewModel(PrinterChangeViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.setTxtNotFoundPrinter(getString(R.string.printer_not_found))
            return it
        }
    }

}
