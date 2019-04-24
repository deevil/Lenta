package com.lenta.shared.features.settings

import com.lenta.shared.platform.viewmodel.CoreViewModel

abstract class CoreSettingsViewModel : CoreViewModel(){
    abstract fun onClickHome()
    abstract fun onClickExit()
    abstract fun onClickPrinter()
    abstract fun onClickWork()
    abstract fun onClickTechLog()
    abstract fun onBackPressed()
}