package com.lenta.bp10.features.tech_login

import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.features.tech_login.CoreTechLoginFragment
import com.lenta.shared.utilities.extentions.provideViewModel

class TechLoginFragment : CoreTechLoginFragment() {
    override fun getPageNumber(): String = "10/55"

    override fun getViewModel(): TechLoginViewModel {
        provideViewModel(TechLoginViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }
}