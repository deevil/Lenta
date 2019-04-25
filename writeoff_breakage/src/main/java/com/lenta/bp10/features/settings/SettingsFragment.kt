package com.lenta.bp10.features.settings

import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.features.settings.CoreSettingsFragment
import com.lenta.shared.features.settings.CoreSettingsViewModel
import com.lenta.shared.utilities.extentions.provideViewModel

class SettingsFragment : CoreSettingsFragment() {

    override fun getPageNumber(): String  = "10/02"

    override fun getViewModel(): CoreSettingsViewModel {
        provideViewModel(SettingsViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }
}