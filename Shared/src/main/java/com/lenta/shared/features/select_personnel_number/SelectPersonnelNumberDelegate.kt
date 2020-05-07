package com.lenta.shared.features.select_personnel_number

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.requests.network.PersonnelNumberNetRequest
import com.lenta.shared.requests.network.TabNumberInfo
import com.lenta.shared.requests.network.TabNumberParams
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectPersonnelNumberDelegate @Inject constructor(
        private var personnelNumberNetRequest: PersonnelNumberNetRequest,
        private var sessionInfo: ISessionInfo,
        private var appSettings: IAppSettings,
        private val coreNavigator: ICoreNavigator
) {

    private lateinit var viewModelScope: () -> CoroutineScope
    private lateinit var onNextScreenOpen: () -> Unit

    lateinit var personnelNumber: MutableLiveData<String>
    lateinit var fullName: MutableLiveData<String>
    lateinit var employeesPosition: MutableLiveData<String>
    lateinit var editTextFocus: MutableLiveData<Boolean>
    lateinit var nextButtonFocus: MutableLiveData<Boolean>

    var codeConfirm: Int? = null

    val isAppGoodsReceiving: MutableLiveData<Boolean> = MutableLiveData(false)


    fun init(
            viewModelScope: () -> CoroutineScope,
            onNextScreenOpen: () -> Unit
    ) {
        this.viewModelScope = viewModelScope
        this.onNextScreenOpen = onNextScreenOpen

        initWithViewModel()


    }

    private fun initWithViewModel() {
        viewModelScope().launch {
            when {
                sessionInfo.personnelNumber != null && !isAppGoodsReceiving.value!! -> { //https://trello.com/c/Z0wRVpjQ !isAppGoodsReceiving.value!!-это условие добавлено для приложения Приемка (goods_receiving)
                    personnelNumber.value = sessionInfo.personnelNumber
                    fullName.value = sessionInfo.personnelFullName
                    searchPersonnelNumber()
                }
                appSettings.lastPersonnelNumber != null && !isAppGoodsReceiving.value!! -> { //https://trello.com/c/Z0wRVpjQ !isAppGoodsReceiving.value!!-это условие добавлено для приложения Приемка (goods_receiving)
                    personnelNumber.value = appSettings.lastPersonnelNumber
                    searchPersonnelNumber()
                }
                else -> editTextFocus.postValue(true)
            }
        }

    }


    private fun searchPersonnelNumber() {
        Logg.d { "searchPersonnelNumber" }
        viewModelScope().launch {
            coreNavigator.showProgress(personnelNumberNetRequest)
            personnelNumberNetRequest(TabNumberParams(tabNumber = personnelNumber.value
                    ?: "")).either(::handleFailure, ::handleSuccess)
            coreNavigator.hideProgress()
        }
    }

    private fun handleSuccess(personnelNumberInfo: TabNumberInfo) {
        Logg.d { "handleSuccess $personnelNumberInfo" }
        fullName.value = personnelNumberInfo.name
        employeesPosition.value = personnelNumberInfo.jobName
        viewModelScope().launch {
            nextButtonFocus.postValue(true)
        }


    }

    fun handleFailure(failure: Failure) {
        if (isAppGoodsReceiving.value!!) {
            fullName.value = ""
            employeesPosition.value = ""
            coreNavigator.openAlertScreen(failure, timeAutoExitInMillis = 3000)
        } else {
            coreNavigator.openAlertScreen(failure)
        }
    }

    fun onOkInSoftKeyboard(): Boolean {
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
            coreNavigator.goBackWithResultCode(it)
            return
        }
        onNextScreenOpen()
    }

    fun onResume() {
        if (this::viewModelScope.isInitialized) {
            viewModelScope().launch {
                if (personnelNumber.value.isNullOrBlank()) {
                    editTextFocus.value = true
                } else {
                    nextButtonFocus.value = true
                }
            }
        }

    }


    fun onScanResult(data: String) {
        personnelNumber.value = data
        searchPersonnelNumber()
    }


}