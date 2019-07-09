package com.lenta.bp7.features.check_type

import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class CheckTypeViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo

    companion object {
        const val SELF_CONTROL = "Самоконтроль ТК"
        const val EXTERNAL_AUDIT = "Внешний аудит"
    }

    fun onClickSelfControl() {
        sessionInfo.checkType = SELF_CONTROL
        navigator.openCodeScreen()
    }

    fun onClickExternalAudit() {
        sessionInfo.checkType = EXTERNAL_AUDIT
        navigator.openCodeScreen()
    }
}
