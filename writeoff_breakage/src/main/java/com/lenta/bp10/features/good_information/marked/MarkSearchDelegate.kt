package com.lenta.bp10.features.good_information.marked

import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.db.ProductInfoDbRequest
import com.lenta.bp10.requests.network.ExciseStampNetRequest
import com.lenta.bp10.requests.network.MarkedInfoNetRequest
import com.lenta.bp10.requests.network.MarkedInfoParams
import com.lenta.bp10.requests.network.MarkedInfoResult
import com.lenta.bp10.requests.network.pojo.MarkInfo
import com.lenta.bp10.requests.network.pojo.Property
import com.lenta.shared.exception.Failure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MarkSearchDelegate @Inject constructor(
        private val navigator: IScreenNavigator,
        private val productInfoDbRequest: ProductInfoDbRequest,
        private val exciseStampNetRequest: ExciseStampNetRequest,
        private val markedInfoNetRequest: MarkedInfoNetRequest
) {

    private lateinit var viewModelScope: () -> CoroutineScope

    //private lateinit var tkNumber: String

    private lateinit var material: String

    private lateinit var updateProperties: (properties: List<Property>) -> Unit

    private lateinit var handleScannedMark: (mark: String) -> Unit

    private lateinit var handleScannedBox: (marks: List<MarkInfo>) -> Unit


    fun init(viewModelScope: () -> CoroutineScope,
             material: String,
             handleScannedMark: (mark: String) -> Unit,
             handleScannedBox: (marks: List<MarkInfo>) -> Unit,
             updateProperties: (properties: List<Property>) -> Unit) {

        this.viewModelScope = viewModelScope
        this.material = material
        this.updateProperties = updateProperties
        this.handleScannedMark = handleScannedMark
        this.handleScannedBox = handleScannedBox
    }

    fun searchExciseStamp(code: String) {
        /*actionByNumber(
                number = code,
                funcForShoes = { ean, correctedNumber, originalNumber ->
                    requestMarkInfo(correctedNumber)
                },
                funcForCigarettes = ::requestBoxInfo,
                funcForCigarettesBox = ::requestBoxInfo,
                funcForNotValidFormat = navigator::showIncorrectEanFormat
        )*/


        /*if (!(code.length == 68 || code.length == 150)) {
            navigator.openAlertNotValidFormatStamp()
            return
        }

        viewModelScope().launch {
            navigator.showProgress(exciseStampNetRequest)

            exciseStampNetRequest(ExciseStampParams(
                    pdf417 = code,
                    werks = tkNumber,
                    matnr = materialNumber))
                    .either(::handleFailure, ::handleExciseStampSuccess)

            navigator.hideProgress()
        }*/


    }

    fun requestMarkInfo(number: String) {
        viewModelScope().launch {
            navigator.showProgress(markedInfoNetRequest)
            markedInfoNetRequest(MarkedInfoParams(
                    taskNumber = "",
                    material = material,
                    markNumber = number
            )).either(::handleFailure) { result ->
                handleMarkRequestResult(number, result)
            }
        }
    }

    fun requestBoxInfo(number: String) {
        viewModelScope().launch {
            navigator.showProgress(markedInfoNetRequest)
            markedInfoNetRequest(MarkedInfoParams(
                    taskNumber = "",
                    material = material,
                    boxNumber = number
            )).either(::handleFailure) { result ->
                handleBoxRequestResult(result)
            }
        }
    }

    private fun handleFailure(failure: Failure) {
        navigator.openAlertScreen(failure)
    }

    private fun handleMarkRequestResult(markNumber: String, result: MarkedInfoResult) {
        if (result.status == "0") {
            updateProperties.invoke(result.properties.orEmpty())
            handleScannedMark.invoke(markNumber)
        } else {
            navigator.openInfoScreen(result.errorText)
        }
    }

    private fun handleBoxRequestResult(result: MarkedInfoResult) {
        if (result.status == "100") {
            updateProperties.invoke(result.properties.orEmpty())
            handleScannedBox(result.marks.orEmpty())
        } else {
            navigator.openInfoScreen(result.errorText)
        }
    }


    /*fun handleResult(code: Int?): Boolean {
        if (code == requestCodeAddBadStamp) {
            val isBadStamp = true
            handleScannedMark(isBadStamp)
            return true
        }
        return false
    }*/

    /*private fun handleExciseStampSuccess(exciseStampRestInfo: ExciseStampRestInfo) {
        val retCode = exciseStampRestInfo.retCode
        val serverDescription = exciseStampRestInfo.errorText

        when (retCode) {
            0 -> {
                val isBadStamp = false
                handleScannedMark(isBadStamp)
            }
            2 -> {
                navigator.openStampAnotherMarketAlert(requestCodeAddBadStamp)
            }
            1 -> {
                viewModelScope().launch {
                    navigator.showProgress(productInfoDbRequest)
                    productInfoDbRequest(ProductInfoRequestParams(number = exciseStampRestInfo.matNr))
                            .either(::handleFailure, ::openAlertForAnotherProductStamp)
                    navigator.hideProgress()

                }

            }
            else -> navigator.openInfoScreen(serverDescription)
        }

    }*/


    /*private fun openAlertForAnotherProductStamp(productInfo: ProductInfo) {
        navigator.openAnotherProductStampAlert(productName = productInfo.description)
    }*/

}