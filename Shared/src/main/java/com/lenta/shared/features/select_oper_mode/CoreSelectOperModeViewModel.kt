package com.lenta.shared.features.select_oper_mode

import com.lenta.shared.platform.viewmodel.CoreViewModel

abstract class CoreSelectOperModeViewModel : CoreViewModel() {
    abstract fun onClickBack()
    abstract fun onClickExit()
    abstract fun onClickTestEnvir()
    abstract fun onClickWorkEnvir()
    abstract fun onClickSettingsConnections()
}