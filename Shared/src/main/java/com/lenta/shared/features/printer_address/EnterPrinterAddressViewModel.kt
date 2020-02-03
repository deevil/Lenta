package com.lenta.shared.features.printer_address

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class EnterPrinterAddressViewModel : CoreViewModel() {

    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var navigator: ICoreNavigator

    val printerIpAddressField = MutableLiveData("")

    val applyButtonEnabled = printerIpAddressField.map {
        it?.isNotEmpty()
    }

    init {
        viewModelScope.launch {
            printerIpAddressField.value = sessionInfo.printer
        }
    }

    fun onClickApply() {
        sessionInfo.printer = printerIpAddressField.value
        navigator.goBack()
    }

}
