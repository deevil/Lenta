package com.lenta.bp18.features.select_good

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.lenta.bp18.features.other.SendDataViewModel
import com.lenta.bp18.platform.Constants
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class SelectGoodViewModel : SendDataViewModel() {

    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var appSettings: IAppSettings

    private val weightValue = listOf("23", "24", "27", "28")

    val barcodeField:MutableLiveData<String> = MutableLiveData()

    val nextButtonEnabled = barcodeField.map { !it.isNullOrBlank() }
    val requestFocusToBarcode = MutableLiveData<Boolean>(true)

    fun onClickNext() {
        launchUITryCatch {
            ean.value = barcodeField.value ?: Constants.GOOD_BARCODE
            val barcode = ean.value.toString()
            var weight: String? = "0"
            if (weightValue.contains(barcode.substring(0 until 2))) {
                ean.value = barcode.replace(barcode.takeLast(6), "000000")
                weight = barcode.takeLast(6).take(5)
            }
            searchEan(ean.value.toString(), weight)
        }
    }

    private fun searchEan(ean: String, weight: String?) {
        launchUITryCatch {
            when (val good = database.getGoodByEan(ean)) {
                null -> showError()
                else -> {
                    val goodInfo = Bundle()
                    goodInfo.putString("EAN", good.ean)
                    goodInfo.putString("MATERIAL", good.getFormattedMaterial())
                    goodInfo.putString("NAME", good.name)
                    openGoodInfoScreen(goodInfo,weight)
                }
            }
        }
    }

    private fun openGoodInfoScreen(goodInfo: Bundle, weight: String? ) {
        navigator.openGoodsInfoScreen(goodInfo, weight)
    }

    private fun showError() {
        navigator.showAlertGoodsNotFound()
    }

    fun onClickExit() {
        /*Выход из приложения*/
    }
}