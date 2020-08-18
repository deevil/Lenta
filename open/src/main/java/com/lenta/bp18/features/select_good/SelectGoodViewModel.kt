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
import com.lenta.shared.utilities.Logg
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
    val requestFocusToBarcode = MutableLiveData<Boolean>(false)

    fun onClickNext() {
        ean.value = barcodeField.value ?: Constants.GOOD_BARCODE
        preparationEanForSearch()
    }

    fun onScanResult(data: String) {
        barcodeField.value = data
        ean.value = data
        preparationEanForSearch()
    }

    private fun preparationEanForSearch() = launchUITryCatch {
        navigator.showProgress(context.getString(R.string.load_barcode_data))
        var barcode = ean.value.toString()
        var weight = DEFAULT_WEIGHT

        if (barcode.length >= MINIMUM_GS1_CODE_LENGTH) {
            val ean128Barcode = withContext(Dispatchers.Default) {
                EAN128Parser.parseWith(barcode, EAN128Parser.EAN_01)
            }
            if (ean128Barcode != null) {
                barcode = ean128Barcode
            }
        } else {
            Logg.d { "----->  barcode EAN 128 less than 16 chars" }
        }

        val weightCollider = barcode.substring(0 until 3)
        if (weightValue.any { weightCollider.contains(it) }) {
            val changedBarcode = barcode.replace(barcode.takeLast(6), TAKEN_ZEROS)
            weight = barcode.takeLast(6).take(5)
            barcode = changedBarcode
        }

        ean.value = barcode
        searchEan(barcode, weight)
    }

    private suspend fun searchEan(ean: String, weight: String) {
        val goodWithEan = database.getGoodByEan(ean)
        goodWithEan?.let { good ->
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
    }
}