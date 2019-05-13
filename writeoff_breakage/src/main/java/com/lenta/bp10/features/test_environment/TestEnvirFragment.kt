package com.lenta.bp10.features.test_environment

import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.features.test_environment.CoreTestEnvirFragment
import com.lenta.shared.features.test_environment.CoreTestEnvirViewModel
import com.lenta.shared.utilities.extentions.provideViewModel

class TestEnvirFragment : CoreTestEnvirFragment() {
    override fun getPageNumber(): String = "10/56"

    override fun getViewModel(): CoreTestEnvirViewModel {
        provideViewModel(TestEnvirViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }
}