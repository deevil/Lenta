package com.lenta.bp10.features.select_oper_mode

import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.features.select_oper_mode.CoreSelectOperModeFragment
import com.lenta.shared.features.select_oper_mode.CoreSelectOperModeViewModel
import com.lenta.shared.utilities.extentions.provideViewModel

class SelectOperModeFragment : CoreSelectOperModeFragment() {
    override fun getPageNumber(): String = "10/54"

    override fun getViewModel(): CoreSelectOperModeViewModel {
        provideViewModel(SelectOperModeViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }
}