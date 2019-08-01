package com.lenta.bp7.features.select_check_type

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.CheckType
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectCheckTypeViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var checkData: CheckData


    val selfControlButtonEnabled: MutableLiveData<Boolean> = MutableLiveData(true)
    val externalAuditButtonEnabled: MutableLiveData<Boolean> = MutableLiveData(true)

    init {
        viewModelScope.launch {
            if (checkData.segments.isNotEmpty()) {
                when (checkData.checkType) {
                    CheckType.SELF_CONTROL -> {
                        selfControlButtonEnabled.value = true
                        externalAuditButtonEnabled.value = false
                    }
                    CheckType.EXTERNAL_AUDIT -> {
                        selfControlButtonEnabled.value = false
                        externalAuditButtonEnabled.value = true
                    }
                }
            }
        }
    }

    fun onClickSelfControl() {
        checkData.checkType = CheckType.SELF_CONTROL
        navigator.openCodeScreen()
    }

    fun onClickExternalAudit() {
        checkData.checkType = CheckType.EXTERNAL_AUDIT
        navigator.openCodeScreen()
    }

    fun onClickExit() {
        navigator.showDoYouReallyWantToLeave {
            if (checkData.isExistUnsentData()) {
                navigator.showUnsentDataDetected(
                        exitToAppCallback = { navigator.finishApp() },
                        goOverCallback = { navigator.openSegmentListScreen() }
                )
            } else {
                navigator.finishApp()
            }
        }
    }
}
