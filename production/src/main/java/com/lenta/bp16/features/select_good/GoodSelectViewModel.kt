package com.lenta.bp16.features.select_good

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.movement.params.ProductInfoParams
import com.lenta.bp16.model.movement.result.ProductInfoResult
import com.lenta.bp16.model.movement.ui.ProducerUI
import com.lenta.bp16.model.pojo.GoodParams
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.ProductInfoNetRequest
import com.lenta.bp16.request.pojo.Ean
import com.lenta.bp16.request.pojo.ProducerInfo
import com.lenta.bp16.request.pojo.Product
import com.lenta.bp16.request.pojo.ProductInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import javax.inject.Inject

class GoodSelectViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var productInfoNetRequest: ProductInfoNetRequest

    @Inject
    lateinit var sessionInfo: ISessionInfo

    val deviceIp = MutableLiveData("")

    val marketNumber by unsafeLazy { sessionInfo.market }

    val weightValueList = listOf(VALUE_23, VALUE_24, VALUE_27, VALUE_28)

    val enteredEanField = MutableLiveData("")
    val requestFocusEnteredEanField = MutableLiveData(true)

    private val productInfo: MutableLiveData<ProductInfo> by unsafeLazy {
        MutableLiveData<ProductInfo>()
    }

    private val producerInfo: MutableLiveData<ProducerInfo> by unsafeLazy {
        MutableLiveData<ProducerInfo>()
    }

    val enabledNextButton = enteredEanField.map { !it.isNullOrBlank() }

    private fun searchGood() {
        val ean: MutableList<String> = mutableListOf()
        var weight = DEF_WEIGHT
        var barcode = enteredEanField.value.orEmpty()
        val firstCode = barcode.substring(0 until 2)
        if (weightValueList.contains(firstCode)) {
            weight = barcode.takeLast(6).take(5).toDouble()
            val changedBarcode = barcode.replace(barcode.takeLast(6), TAKEN_ZEROS)
            barcode = changedBarcode
        }
        ean.add(barcode)
        val matnr: List<String> = mutableListOf()
        launchUITryCatch {
            productInfoNetRequest(
                    ProductInfoParams(
                            ean = ean.map { Ean(it) },
                            matnr = matnr.map { Product(it) }
                    ).also { navigator.hideProgress() }
            ).either(::handleFailure) { productInfoResult ->
                productInfo.value = productInfoResult.product?.getOrNull(0)
                producerInfo.value = productInfoResult.producers?.getOrNull(0)
                Unit
            }
            productInfo.value?.let { goodInfo ->
                val good = GoodParams(
                        ean = goodInfo.ean.orEmpty(),
                        material = goodInfo.getFormattedMaterial().orEmpty(),
                        name = goodInfo.productName.orEmpty(),
                        weight = weight,
                        zPart = goodInfo.isPart.orEmpty().isNotEmpty(),
                        uom = goodInfo.uom.orEmpty(),
                        umrez = goodInfo.umrez.orEmpty(),
                        umren = goodInfo.umren.orEmpty(),
                        producers = ProducerUI(
                                producerCode = producerInfo.value?.prodCode.orEmpty(),
                                producerName = producerInfo.value?.prodName.orEmpty()
                        )
                )
                navigator.openGoodInfoScreen(good)
            } ?: navigator.showAlertGoodNotFound()
        }
    }

    fun onClickNext() {
        navigator.showProgressLoadingData()
        searchGood()
    }

    fun onScanResult(data: String) {
        enteredEanField.value = data
        searchGood()
    }

    fun onClickMenu() {
        navigator.openMainMenuScreen()
    }

    companion object {
        const val VALUE_23 = "23"
        const val VALUE_24 = "24"
        const val VALUE_27 = "27"
        const val VALUE_28 = "28"

        private const val DEF_WEIGHT = 0.0
        private const val TAKEN_ZEROS = "000000"
    }

}