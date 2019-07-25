package com.lenta.bp7.features.check_type

import com.lenta.bp7.data.CheckType
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class CheckTypeViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var checkData: CheckData

    fun onClickSelfControl() {
        checkData.checkType = CheckType.SELF_CONTROL
        navigator.openCodeScreen()
    }

    fun onClickExternalAudit() {
        checkData.checkType = CheckType.EXTERNAL_AUDIT
        navigator.openCodeScreen()
    }
}
