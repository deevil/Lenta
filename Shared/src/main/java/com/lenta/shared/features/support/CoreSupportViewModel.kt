package com.lenta.shared.features.support

import com.lenta.shared.platform.viewmodel.CoreViewModel

abstract class CoreSupportViewModel : CoreViewModel() {
    abstract fun onClickBack()
    abstract fun onBackPressed()
}