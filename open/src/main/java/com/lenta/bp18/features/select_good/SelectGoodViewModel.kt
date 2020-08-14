package com.lenta.bp18.features.select_good

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp18.R
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
import com.lenta.shared.utilities.extentions.unsafeLazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    @Inject
    lateinit var context: Context

    private val ean: MutableLiveData<String> = MutableLiveData()

    private val weightValue by unsafeLazy { listOf(VALUE_23, VALUE_24, VALUE_27, VALUE_28) }

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

    private fun preparationEanForSearch() = launchUITryCatch {
        navigator.showProgress(context.getString(R.string.load_barcode_data))
        //00030000020005
        val barcode = ean.value.toString()
        var weight = DEFAULT_WEIGHT

        withContext(Dispatchers.IO) {
            if (weightValue.contains(barcode.substring(0 until 2))) {
                ean.postValue(barcode.replace(barcode.takeLast(6), TAKEN_ZEROS))
                weight = barcode.takeLast(6).take(5)
            } else {
                if (barcode.length >= MINIMUM_GS1_CODE_LENGTH) {
                    val ean128Barcode = EAN128Parser.parse(barcode, false).entries.find { pair ->
                        pair.key.AI == EAN_01 || pair.key.AI == EAN_02
                    }?.value

                    ean.postValue(ean128Barcode.orEmpty())
                } else if (barcode.length != MINIMUM_GS1_CODE_LENGTH) {
                    ean.postValue("")
                    println("----->  barcode EAN 128 less than 16 chars")
                }
            }
        }

        searchEan(ean.value.toString(), weight)
    }

    private suspend fun searchEan(ean: String, weight: String) {
        database.getGoodByEan(ean)?.let { good ->
            val goodMaterial = good.getFormattedMaterial()
            val goodParams = GoodParams(
                    ean = good.ean,
                    material = goodMaterial,
                    name = good.name,
                    weight = weight
            )

            with(navigator) {
                hideProgress()
                openGoodsInfoScreen(goodParams)
            }
        } ?: showAlertForNotFoundedGood()
    }

    private fun showAlertForNotFoundedGood() {
        with(navigator) {
            hideProgress()
            showAlertGoodsNotFound()
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