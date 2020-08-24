package com.lenta.bp16.features.select_good

import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.movement.params.ProductInfoParams
import com.lenta.bp16.model.movement.result.ProductInfoResult
import com.lenta.bp16.model.pojo.GoodParams
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.DatabaseRepository
import com.lenta.bp16.request.ProductInfoNetRequest
import com.lenta.bp16.request.pojo.Ean
import com.lenta.bp16.request.pojo.ProducerInfo
import com.lenta.bp16.request.pojo.Product
import com.lenta.bp16.request.pojo.ProductInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
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

    private val productInfo : MutableLiveData<ProductInfo> by unsafeLazy {
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
        }
    }


    fun onClickNext() {
        searchGood()
        val goodInfo = productInfo.value?.let {goodInfo ->
            val good = GoodParams(
                    ean = goodInfo.ean,
                    material = goodInfo.productMatcode,
                    name = goodInfo.productName
            )
        }

    }

    fun onScanResult(data: String) {
        enteredEanField.value = data
        searchGood()
    }

    fun onClickMenu() {
        navigator.openMainMenuScreen()
    }

}