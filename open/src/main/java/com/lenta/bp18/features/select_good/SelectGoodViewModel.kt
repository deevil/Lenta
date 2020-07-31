package com.lenta.bp18.features.select_good

import androidx.lifecycle.MutableLiveData
import com.lenta.bp18.features.other.SendDataViewModel
import com.lenta.bp18.model.pojo.GoodParams
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
        ean.value = barcodeField.value ?: Constants.GOOD_BARCODE
        preparationEanForSearch()
    }

    fun onScanResult(data: String) {
        ean.value = data
        preparationEanForSearch()
    }

    private fun preparationEanForSearch() {
        val barcode = ean.value.toString()
        var weight = "0"
        if (weightValue.contains(barcode.substring(0 until 2))) {
            ean.value = barcode.replace(barcode.takeLast(6), "000000")
            weight = barcode.takeLast(6).take(5)
        }
        searchEan(ean.value.toString(), weight)
    }

    private fun searchEan(ean: String, weight: String) {
        launchUITryCatch {
            when (val good = database.getGoodByEan(ean)) {
                null -> navigator.showAlertGoodsNotFound()
                else -> {
                    val goodParams = GoodParams(ean = good.ean,
                            material = good.getFormattedMaterial(),
                            name = good.name,
                            weight = weight)
                    navigator.openGoodsInfoScreen(goodParams)
                }
            }
        }
    }

    companion object {
        const val VALUE_23 = "23"
        const val VALUE_24 = "24"
        const val VALUE_27 = "27"
        const val VALUE_28 = "28"
    }
}