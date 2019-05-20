package com.lenta.shared.features.auxiliary_menu

import com.lenta.shared.platform.viewmodel.CoreViewModel

abstract class CoreAuxiliaryMenuViewModel : CoreViewModel() {
    abstract fun onClickHome()
    abstract fun onClickSettings()
    abstract fun onClickSupport()
    abstract fun onBackPressed()
}