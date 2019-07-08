package com.lenta.bp7.features.check_type

import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg

class CheckTypeViewModel : CoreViewModel() {


    fun onClickSelfControl() {
        Logg.d { "-------> onClickSelfControl" }
    }

    fun onClickExternalAudit() {
        Logg.d { "-------> onClickExternalAudit" }
    }
}
