package com.lenta.bp10.features.select_personnel_number

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.network.PersonnelNumberNetRequest
import com.lenta.bp10.requests.network.TabNumberInfo
import com.lenta.bp10.requests.network.TabNumberParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectPersonnelNumberViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {
    @Inject
    lateinit var personnelNumberNetRequest: PersonnelNumberNetRequest
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo

    val personnelNumber = MutableLiveData<String>("")
    val fio = MutableLiveData<String>("")
    val employeesPosition = MutableLiveData<String>("")

    private fun searchPersonnelNumber() {
        Logg.d { "searchPersonnelNumber" }
        viewModelScope.launch {
            screenNavigator.showProgress(personnelNumberNetRequest)
            personnelNumberNetRequest(TabNumberParams(tabNumber = personnelNumber.value
                    ?: "")).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(personnelNumberInfo: TabNumberInfo) {
        Logg.d { "handleSuccess $personnelNumberInfo" }
        fio.value = personnelNumberInfo.name
        employeesPosition.value = personnelNumberInfo.jobName

    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchPersonnelNumber()
        return true
    }

    fun onClickNext() {

        sessionInfo.personnelNumber = if (!fio.value.isNullOrEmpty()) personnelNumber.value else null
        screenNavigator.openMainMenuScreen()
    }
}
