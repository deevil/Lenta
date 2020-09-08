package com.lenta.bp10.features.good_information.marked

import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.network.MarkedInfoNetRequest
import com.lenta.bp10.requests.network.MarkedInfoParams
import com.lenta.bp10.requests.network.MarkedInfoResult
import com.lenta.bp10.requests.network.pojo.MarkInfo
import com.lenta.bp10.requests.network.pojo.Property
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MarkSearchDelegate @Inject constructor(
        private val navigator: IScreenNavigator,
        private val markedInfoNetRequest: MarkedInfoNetRequest
) {

    private lateinit var viewModelScope: () -> CoroutineScope

    private lateinit var tkNumber: String

    private var productInfo: ProductInfo? = null

    private lateinit var updateProperties: (properties: List<Property>) -> Unit

    private lateinit var handleScannedMark: (mark: String) -> Unit

    private lateinit var handleScannedBox: (marks: List<MarkInfo>) -> Unit


    fun init(viewModelScope: () -> CoroutineScope,
             tkNumber: String,
             productInfo: ProductInfo?,
             handleScannedMark: (mark: String) -> Unit,
             handleScannedBox: (marks: List<MarkInfo>) -> Unit,
             updateProperties: (properties: List<Property>) -> Unit) {

        this.viewModelScope = viewModelScope
        this.tkNumber = tkNumber
        this.productInfo = productInfo
        this.updateProperties = updateProperties
        this.handleScannedMark = handleScannedMark
        this.handleScannedBox = handleScannedBox
    }

    fun requestShoesMarkInfo(number: String) {
        viewModelScope().launch {
            navigator.showProgress(markedInfoNetRequest)
            markedInfoNetRequest(MarkedInfoParams(
                    tkNumber = tkNumber,
                    material = productInfo?.materialNumber.orEmpty(),
                    markNumber = number,
                    markType = productInfo?.markType.orEmpty()
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { result ->
                handleMarkRequestResult(number, result)
            }
        }
    }

    fun requestCigaretteMarkInfo(number: String) {
        viewModelScope().launch {
            navigator.showProgress(markedInfoNetRequest)
            markedInfoNetRequest(MarkedInfoParams(
                    tkNumber = tkNumber,
                    material = productInfo?.materialNumber.orEmpty(),
                    packNumber = number,
                    markType = productInfo?.markType.orEmpty()
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { result ->
                handleMarkRequestResult(number, result)
            }
        }
    }

    fun requestCigaretteBoxInfo(number: String) {
        viewModelScope().launch {
            navigator.showProgress(markedInfoNetRequest)
            markedInfoNetRequest(MarkedInfoParams(
                    tkNumber = tkNumber,
                    material = productInfo?.materialNumber.orEmpty(),
                    boxNumber = number,
                    markType = productInfo?.markType.orEmpty()
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { result ->
                handleBoxRequestResult(result)
            }
        }
    }

    private fun handleFailure(failure: Failure) {
        navigator.openAlertScreen(failure)
    }

    private fun handleMarkRequestResult(markNumber: String, result: MarkedInfoResult) {
        if (result.status == MARK_CODE_OK) {
            updateProperties.invoke(result.properties.orEmpty())
            handleScannedMark.invoke(markNumber)
        } else {
            navigator.openInfoScreen(result.errorText)
        }
    }

    private fun handleBoxRequestResult(result: MarkedInfoResult) {
        if (result.status == BOX_CODE_OK) {
            updateProperties.invoke(result.properties.orEmpty())
            handleScannedBox(result.marks.orEmpty())
        } else {
            navigator.openInfoScreen(result.errorText)
        }
    }

    companion object {
        private const val MARK_CODE_OK = "0"
        private const val BOX_CODE_OK = "100"
    }

}