package com.lenta.bp18.features.select_good

import androidx.lifecycle.MutableLiveData
import com.lenta.bp18.model.pojo.GoodParams
import com.lenta.bp18.platform.Constants
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IDatabaseRepo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.EAN128Parser
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class SelectGoodViewModel : CoreViewModel() {

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var database: IDatabaseRepo

    val ean: MutableLiveData<String> = MutableLiveData()

    private val weightValue = listOf(VALUE_23, VALUE_24, VALUE_27, VALUE_28)

    val barcodeField: MutableLiveData<String> = MutableLiveData()

    val nextButtonEnabled = barcodeField.map { !it.isNullOrBlank() }
    val requestFocusToBarcode = MutableLiveData<Boolean>(true)

    fun onClickNext() {
        ean.value = Constants.GOOD_BARCODE// barcodeField.value ?: Constants.GOOD_BARCODE
        preparationEanForSearch()
    }

    fun onScanResult(data: String) {
        ean.value = data
        preparationEanForSearch()
    }

    private fun preparationEanForSearch() {
        val barcode = ean.value.toString()
        var weight = DEFAULT_WEIGHT
        if (weightValue.contains(barcode.substring(0 until 2))) {
            ean.value = barcode.replace(barcode.takeLast(6), TAKEN_ZEROS)
            weight = barcode.takeLast(6).take(5)
        } else {
            if (barcode.length >= MINIMUM_GS1_CODE_LENGTH) {
                val ean128Barcode = EAN128Parser.parse(barcode, false).entries.find { pair ->
                    pair.key.AI == EAN_01 || pair.key.AI == EAN_02
                }?.value

                if (ean128Barcode != null) {
                    ean.value = ean128Barcode
                } else if (barcode.length != MINIMUM_GS1_CODE_LENGTH) {
                    println("----->  barcode EAN 128 less than 16 chars")
                }
            }
        }
        searchEan(ean.value.toString(), weight)
    }

    private fun searchEan(ean: String, weight: String) {
        launchUITryCatch {
            val good = database.getGoodByEan(ean)
            good?.let {
                val goodParams = GoodParams(ean = good.ean,
                        material = good.getFormattedMaterial(),
                        name = good.name,
                        weight = weight)
                navigator.openGoodsInfoScreen(goodParams)
            } ?: navigator.showAlertGoodsNotFound()
        }
    }

    companion object {
        const val VALUE_23 = "23"
        const val VALUE_24 = "24"
        const val VALUE_27 = "27"
        const val VALUE_28 = "28"

        private const val MINIMUM_GS1_CODE_LENGTH = 16
        private const val TAKEN_ZEROS = "000000"
        private const val DEFAULT_WEIGHT = "0"
        private const val EAN_01 = "01"
        private const val EAN_02 = "02"
    }
}