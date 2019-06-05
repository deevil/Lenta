package com.lenta.bp10.features.select_personnel_number

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.models.IPersistWriteOffTask
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.network.PersonnelNumberNetRequest
import com.lenta.shared.requests.network.TabNumberInfo
import com.lenta.shared.requests.network.TabNumberParams
import com.lenta.shared.settings.IAppSettings
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
    @Inject
    lateinit var appSettings: IAppSettings
    @Inject
    lateinit var persistWriteOffTask: IPersistWriteOffTask

    val personnelNumber = MutableLiveData<String>("")
    val fullName = MutableLiveData<String>("")
    val employeesPosition = MutableLiveData<String>("")

    private var codeConfirm: Int? = null

    fun setCodeConfirm(codeConfirm: Int?){
        this.codeConfirm = codeConfirm
    }

    init {
        viewModelScope.launch {
            if (sessionInfo.personnelNumber != null) {
                personnelNumber.value = sessionInfo.personnelNumber
                fullName.value = sessionInfo.personnelFullName
            } else {
                personnelNumber.value = appSettings.lastPersonnelNumber
                fullName.value = appSettings.lastPersonnelFullName
            }

        }
    }

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
        fullName.value = personnelNumberInfo.name
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
        (if (!fullName.value.isNullOrEmpty()) personnelNumber.value else null).let {
            sessionInfo.personnelNumber = it
            sessionInfo.personnelFullName = fullName.value

            appSettings.lastPersonnelNumber = it
            appSettings.lastPersonnelFullName = fullName.value
        }

        codeConfirm?.let {
            screenNavigator.goBackWithResultCode(it)
            return
        }

        persistWriteOffTask.getSavedWriteOffTask().let {
            if (it == null || it.taskDescription.tkNumber != sessionInfo.market) {
                screenNavigator.openMainMenuScreen()
            } else {
                screenNavigator.openDetectionSavedDataScreen()
            }
        }

    }

    fun onScanResult(data: String) {
        personnelNumber.value = data
        searchPersonnelNumber()
    }

}
