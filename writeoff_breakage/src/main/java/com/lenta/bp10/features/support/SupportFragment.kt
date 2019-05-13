package com.lenta.bp10.features.support

import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.features.support.CoreSupportFragment
import com.lenta.shared.features.support.CoreSupportViewModel
import com.lenta.shared.utilities.extentions.provideViewModel

class SupportFragment : CoreSupportFragment() {
    override fun getPageNumber(): String = "10/52"

    override fun getViewModel(): CoreSupportViewModel {
        provideViewModel(SupportViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }
}