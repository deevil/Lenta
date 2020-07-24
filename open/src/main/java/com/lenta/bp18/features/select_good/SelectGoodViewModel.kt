package com.lenta.bp18.features.select_good

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp18.features.other.SendDataViewModel
import com.lenta.bp18.platform.Constants
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.settings.IAppSettings
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectGoodViewModel : SendDataViewModel() {

    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var appSettings: IAppSettings

    private val weightValue = listOf("23", "24", "27", "28")

    val barcodeField:MutableLiveData<String> = MutableLiveData()
    val requestFocusToBarcode = MutableLiveData<Boolean>(true)

    fun onClickNext() {
        viewModelScope.launch {
            ean.value = barcodeField.value ?: Constants.GOOD_BARCODE
            val barcode = ean.value.toString()
            var weight: String? = null
            if (weightValue.contains(barcode.substring(0 until 2))) {
                ean.value = barcode.replace(barcode.takeLast(6), "000000")
                weight = barcode.takeLast(6).take(5)
            }
            searchEan(ean.value.toString(), weight)
        }
    }

    private fun searchEan(ean: String, weight: String?) {
        viewModelScope.launch {
            when (database.getGoodByEan(ean)) {
                null -> showError()
                else -> openGoodInfoScreen(ean,weight)
            }
        }
    }

    private fun openGoodInfoScreen(ean: String, weight: String?) {
        navigator.openGoodsInfoScreen(ean, weight)
    }

    private fun showError() {
        navigator.showAlertGoodsNotFound()
    }

    fun onClickExit() {
        /*Выход из приложения*/
    }
}