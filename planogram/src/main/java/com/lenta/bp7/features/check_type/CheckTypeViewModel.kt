package com.lenta.bp7.features.check_type

import com.lenta.bp7.data.CheckType
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.account.IPlanogramSessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class CheckTypeViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: IPlanogramSessionInfo

    fun onClickSelfControl() {
        sessionInfo.checkType = CheckType.SELF_CONTROL.type
        navigator.openCodeScreen()
    }

    fun onClickExternalAudit() {
        sessionInfo.checkType = CheckType.EXTERNAL_AUDIT.type
        navigator.openCodeScreen()
    }
}
