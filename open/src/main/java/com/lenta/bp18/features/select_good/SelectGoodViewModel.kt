package com.lenta.bp18.features.select_good

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp18.R
import com.lenta.bp18.model.pojo.GoodParams
import com.lenta.bp18.platform.Constants
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IDatabaseRepo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.BarcodeViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class SelectGoodViewModel : BarcodeViewModel() {

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

    val barcodeField: MutableLiveData<String> = MutableLiveData()
    val nextButtonEnabled = barcodeField.map { !it.isNullOrBlank() }
    val requestFocusToBarcode = MutableLiveData<Boolean>(false)

    fun onClickNext() = launchUITryCatch {
        ean.value = barcodeField.value ?: Constants.GOOD_BARCODE
        preparationEanForSearch(ean.value.orEmpty())
    }

    fun onScanResult(data: String) = launchUITryCatch {
        barcodeField.value = data
        ean.value = data
        preparationEanForSearch(data)
    }

    private fun preparationEanForSearch(barcode: String) = launchUITryCatch {
        navigator.showProgress(context.getString(R.string.load_barcode_data))
        val barcodeData = processBarcode(barcode)
        ean.value = barcodeData.barcodeInfo.barcode
        searchEan(ean.value.orEmpty(), barcodeData.barcodeInfo.weight)
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
}