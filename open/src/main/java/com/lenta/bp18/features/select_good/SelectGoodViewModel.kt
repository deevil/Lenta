package com.lenta.bp18.features.select_good

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp18.features.other.SendDataViewModel
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.settings.IAppSettings
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectGoodViewModel : SendDataViewModel() {

    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var appSettings: IAppSettings

    val barcodeField:MutableLiveData<String> = MutableLiveData("")
    val requestFocusToBarcode = MutableLiveData<Boolean>(true)

    fun onClickNext() {
        viewModelScope.launch {
            ean.value = barcodeField.value ?: "0"
            val barcode = ean.value.toString()
            if (weightValue.contains(barcode.substring(0 until 2))) {
                ean.value = barcode.replace(barcode.takeLast(6), "000000")
            }
            searchEan(ean.value.toString())
        }
    }

    private fun searchEan(ean: String) {
        viewModelScope.launch {
            good.value = database.getGoodByEan(ean)
            when (good.value) {
                null -> showError()
                else -> openGoodInfoScreen(ean)
            }
        }
    }

    private fun openGoodInfoScreen(ean: String) {
        navigator.openGoodsInfoScreen(ean)
    }

    private fun showError() {
        navigator.showAlertGoodsNotFound()
    }

    fun onClickExit() {
        /*Выход из приложения*/
    }
}