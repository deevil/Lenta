package com.lenta.bp18.features.select_good

import android.os.Bundle
import androidx.core.os.bundleOf
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

    private val weightValue = listOf(VALUE_23, VALUE_24, VALUE_27, VALUE_28)

    val barcodeField: MutableLiveData<String> = MutableLiveData()

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
                    val goodInfo =
                            bundleOf(
                                    Constants.GOOD_INFO_EAN to good.ean,
                                    Constants.GOOD_INFO_MATERIAL to good.getFormattedMaterial(),
                                    Constants.GOOD_INFO_NAME to good.name
                            )
                    openGoodInfoScreen(goodInfo, weight)
                }
            }
        }
    }

    private fun openGoodInfoScreen(goodInfo: Bundle, weight: String?) {
        navigator.openGoodsInfoScreen(goodInfo, weight)
    }

    private fun showError() {
        navigator.showAlertGoodsNotFound()
    }

    fun onClickExit() {
        /*Выход из приложения*/
    }

    companion object {
        const val VALUE_23 = "23"
        const val VALUE_24 = "24"
        const val VALUE_27 = "27"
        const val VALUE_28 = "28"
    }
}