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

    val deviceIp = MutableLiveData("")

    val enteredEanField = MutableLiveData("")
    val requestFocusEnteredEanField = MutableLiveData(true)

    private val productInfoResult: MutableLiveData<ProductInfoResult> by unsafeLazy {
        MutableLiveData<ProductInfoResult>()
    }

    private val productInfo: MutableLiveData<ProductInfo> by unsafeLazy {
        MutableLiveData<ProductInfo>()
    }

    private val producerInfo: MutableLiveData<ProducerInfo> by unsafeLazy {
        MutableLiveData<ProducerInfo>()
    }

    val enabledNextButton = enteredEanField.map { !it.isNullOrBlank() }

    private fun searchGood() {
        val ean: MutableList<String> = mutableListOf()
        ean.add(enteredEanField.value.orEmpty())
        val matnr: List<String> = mutableListOf()
        launchUITryCatch {
            productInfoNetRequest(
                    ProductInfoParams(
                            ean = ean.map { Ean(it) },
                            matnr = matnr.map { Product(it) }
                    )
            ).either(::handleFailure, productInfoResult::setValue)
            productInfoResult.value?.let { productInfoResult ->
                productInfo.value = productInfoResult.product?.getOrNull(0)
                producerInfo.value = productInfoResult.producers?.getOrNull(0)
            }
            productInfo.value?.let { goodInfo ->
                val good = GoodParams(
                        ean = goodInfo.ean.orEmpty(),
                        material = goodInfo.getFormattedMaterial().orEmpty(),
                        name = goodInfo.productName.orEmpty(),
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
        searchGood()
    }

    fun onScanResult(data: String) {
        enteredEanField.value = data
        searchGood()
    }

    fun onClickMenu() {
        navigator.openMainMenuScreen()
    }

}