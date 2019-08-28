package com.lenta.bp10.features.good_information.excise_alco

import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.platform.requestCodeAddBadStamp
import com.lenta.bp10.requests.db.ProductInfoDbRequest
import com.lenta.bp10.requests.db.ProductInfoRequestParams
import com.lenta.bp10.requests.network.ExciseStampNetRequest
import com.lenta.bp10.requests.network.ExciseStampParams
import com.lenta.bp10.requests.network.ExciseStampRestInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KFunction1

class ExciseAlcoDelegate @Inject constructor(
        private val screenNavigator: IScreenNavigator,
        private val productInfoDbRequest: ProductInfoDbRequest,
        private val exciseStampNetRequest: ExciseStampNetRequest
) {


    private lateinit var viewModelScope: () -> CoroutineScope
    private lateinit var tkNumber: String
    private lateinit var materialNumber: String
    private lateinit var handleNewStamp: KFunction1<Boolean, Unit>

    fun init(viewModelScope: () -> CoroutineScope,
             handleNewStamp: KFunction1<Boolean, Unit>,
             materialNumber: String,
             tkNumber: String) {
        this.viewModelScope = viewModelScope
        this.handleNewStamp = handleNewStamp
        this.tkNumber = tkNumber
        this.materialNumber = materialNumber
    }


    fun searchExciseStamp(code: String) {

        if (!(code.length == 68 || code.length == 150)) {
            screenNavigator.openAlertNotValidFormatStamp()
            return
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
    }

    fun handleResult(code: Int?): Boolean {
        if (code == requestCodeAddBadStamp) {
            val isBadStamp = true
            handleNewStamp(isBadStamp)
            return true
        }
        return false
    }

    private fun handleExciseStampSuccess(exciseStampRestInfo: ExciseStampRestInfo) {
        val retCode = exciseStampRestInfo.retCode
        val serverDescription = exciseStampRestInfo.errorText

        when (retCode) {
            0 -> {
                val isBadStamp = false
                handleNewStamp(isBadStamp)
            }
            2 -> {
                screenNavigator.openStampAnotherMarketAlert(requestCodeAddBadStamp)
            }
            1 -> {
                viewModelScope().launch {
                    screenNavigator.showProgress(productInfoDbRequest)
                    productInfoDbRequest(ProductInfoRequestParams(number = exciseStampRestInfo.matNr))
                            .either(::handleFailure, ::openAlertForAnotherProductStamp)
                    screenNavigator.hideProgress()

                }

            }
            else -> screenNavigator.openInfoScreen(serverDescription)
        }

    }

    private fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)

    }


    private fun openAlertForAnotherProductStamp(productInfo: ProductInfo) {
        screenNavigator.openAnotherProductStampAlert(productName = productInfo.description)
    }

}