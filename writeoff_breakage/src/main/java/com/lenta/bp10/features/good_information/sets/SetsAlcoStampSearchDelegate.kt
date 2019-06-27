package com.lenta.bp10.features.good_information.sets

import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.platform.requestCodeAddBadStamp
import com.lenta.bp10.requests.db.ProductInfoDbRequest
import com.lenta.bp10.requests.db.ProductInfoRequestParams
import com.lenta.bp10.requests.network.ExciseStampNetRequest
import com.lenta.bp10.requests.network.ExciseStampParams
import com.lenta.bp10.requests.network.ExciseStampRestInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KFunction2

class SetsAlcoStampSearchDelegate @Inject constructor(
        private val screenNavigator: IScreenNavigator,
        private val exciseStampNetRequest: ExciseStampNetRequest,
        private val productInfoDbRequest: ProductInfoDbRequest
) {

    private var isBadStamp = false
    private var matNumber = ""


    private lateinit var viewModelScope: () -> CoroutineScope
    private lateinit var tkNumber: String
    private lateinit var materialNumber: String
    private lateinit var handleNewStamp: KFunction2<Boolean, ProductInfo, Unit>
    private lateinit var components: List<ProductInfo>

    fun init(viewModelScope: () -> CoroutineScope,
             materialNumber: String,
             handleNewStamp: KFunction2<Boolean, ProductInfo, Unit>,
             tkNumber: String,
             components: List<ProductInfo>) {
        this.viewModelScope = viewModelScope
        this.handleNewStamp = handleNewStamp
        this.tkNumber = tkNumber
        this.materialNumber = materialNumber
        this.components = components
    }


    fun searchExciseStamp(code: String): Boolean {

        if (!(code.length == 68 || code.length == 150)) {
            return false
        }

        viewModelScope().launch {
            screenNavigator.showProgress(exciseStampNetRequest)
            exciseStampNetRequest(ExciseStampParams(
                    pdf417 = code,
                    werks = tkNumber,
                    matnr = materialNumber))
                    .either(::handleFailure, ::handleExciseStampSuccess)
            screenNavigator.hideProgress()
        }

        return true
    }

    fun handleResult(code: Int?): Boolean {
        if (code == requestCodeAddBadStamp) {
            searchProduct()
            return true
        }
        return false
    }

    private fun handleExciseStampSuccess(exciseStampRestInfo: List<ExciseStampRestInfo>) {
        val retCode = exciseStampRestInfo[1].data[0][0].toInt()
        val serverDescription = exciseStampRestInfo[1].data[0][1]

        Logg.d { "exciseStampRestInfo: $exciseStampRestInfo" }

        matNumber = exciseStampRestInfo[0].data[0][0]

        if (components.firstOrNull { it.materialNumber == matNumber } != null) {
            when (retCode) {
                0 -> {
                    screenNavigator.openFailDetectComponentForStampScreen()
                }
                2 -> {
                    screenNavigator.openStampAnotherMarketAlert(requestCodeAddBadStamp)
                }
                1 -> {
                    searchProduct()
                }
                else -> screenNavigator.openInfoScreen(serverDescription)
            }
        } else {
            screenNavigator.openFailDetectComponentForStampScreen()
        }

    }

    private fun searchProduct() {
        viewModelScope().launch {
            screenNavigator.showProgress(productInfoDbRequest)
            productInfoDbRequest(ProductInfoRequestParams(number = matNumber))
                    .either(::handleFailure, ::onSearchProductSuccess)
            screenNavigator.hideProgress()

        }
    }


    private fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)

    }


    private fun onSearchProductSuccess(productInfo: ProductInfo) {
        handleNewStamp(isBadStamp, productInfo)
    }

}