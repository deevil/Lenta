package com.lenta.bp10.features.select_tab_number

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.network.TabNumberInfo
import com.lenta.bp10.requests.network.TabNumberNetRequest
import com.lenta.bp10.requests.network.TabNumberParams
import com.lenta.shared.exception.Failure
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectTabNumberViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {
    @Inject
    lateinit var tabNumberNetRequest: TabNumberNetRequest
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    var tabNumber = MutableLiveData<String>("")
    var fio = MutableLiveData<String>("")
    var employeesPosition = MutableLiveData<String>("")

    fun searchTabNumber() {
        Logg.d { "searchTabNumber" }
        viewModelScope.launch {
            screenNavigator.showProgress(tabNumberNetRequest)
            tabNumberNetRequest(TabNumberParams(tabNumber = tabNumber.value
                    ?: "")).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(tabNumberInfo: TabNumberInfo) {
        Logg.d { "handleSuccess $tabNumberInfo" }
        fio.value = tabNumberInfo.name
        employeesPosition.value = tabNumberInfo.jobName

    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchTabNumber()
        return true
    }

    fun onClickNext() {
        searchTabNumber()
    }
}
