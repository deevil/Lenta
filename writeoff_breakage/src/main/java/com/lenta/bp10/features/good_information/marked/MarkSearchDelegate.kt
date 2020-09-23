package com.lenta.bp10.features.good_information.marked

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.network.MarkedInfoNetRequest
import com.lenta.bp10.requests.network.MarkedInfoParams
import com.lenta.bp10.requests.network.MarkedInfoResult
import com.lenta.bp10.requests.network.pojo.MarkInfo
import com.lenta.bp10.requests.network.pojo.Property
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class MarkSearchDelegate @Inject constructor(
        private val navigator: IScreenNavigator,
        private val markedInfoNetRequest: MarkedInfoNetRequest
) : CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private lateinit var isSpecialMode: MutableLiveData<Boolean>

    private lateinit var tkNumber: String

    private var productInfo: ProductInfo? = null

    private lateinit var updateProperties: (properties: List<Property>) -> Unit

    private lateinit var handleScannedMark: (mark: String) -> Unit

    private lateinit var handleScannedBox: (boxNumber: String, marks: List<MarkInfo>) -> Unit


    fun init(isSpecialMode: MutableLiveData<Boolean>,
             tkNumber: String,
             productInfo: ProductInfo?,
             handleScannedMark: (mark: String) -> Unit,
             handleScannedBox: (boxNumber: String, marks: List<MarkInfo>) -> Unit,
             updateProperties: (properties: List<Property>) -> Unit) {

        this.isSpecialMode = isSpecialMode
        this.tkNumber = tkNumber
        this.productInfo = productInfo
        this.updateProperties = updateProperties
        this.handleScannedMark = handleScannedMark
        this.handleScannedBox = handleScannedBox
    }

    fun requestMarkInfo(number: String) {
        launch {
            navigator.showProgress(markedInfoNetRequest)
            markedInfoNetRequest(MarkedInfoParams(
                    tkNumber = tkNumber,
                    material = productInfo?.materialNumber.orEmpty(),
                    markNumber = number,
                    markType = productInfo?.markedGoodType.orEmpty()
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { result ->
                handleMarkRequestResult(number, result)
            }
        }
    }

    fun requestPackInfo(boxNumber: String) {
        launch {
            navigator.showProgress(markedInfoNetRequest)
            markedInfoNetRequest(MarkedInfoParams(
                    tkNumber = tkNumber,
                    material = productInfo?.materialNumber.orEmpty(),
                    packNumber = boxNumber,
                    markType = productInfo?.markedGoodType.orEmpty()
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { result ->
                handleBoxRequestResult(boxNumber, result)
            }
        }
    }

    private fun handleFailure(failure: Failure) {
        navigator.openAlertScreen(failure)
    }

    private fun handleMarkRequestResult(markNumber: String, result: MarkedInfoResult) {
        with(result) {
            val isSpecialMode = isSpecialMode.value ?: false

            val isValidForCommonMode = !isSpecialMode && status == COMMON_MODE_MARK_OK
            val isValidForSpecialMode = isSpecialMode && status == SPECIAL_MODE_MARK_OK
            val isIncorrectMarkMode = isSpecialMode && status == COMMON_MODE_MARK_OK

            when {
                isValidForCommonMode || isValidForSpecialMode -> {
                    updateProperties.invoke(properties.orEmpty())
                    handleScannedMark.invoke(markNumber)
                }
                isIncorrectMarkMode -> navigator.showIncorrectMarkScanMode()
                else -> navigator.openInfoScreen(statusDescription.orEmpty())
            }
        }
    }

    private fun handleBoxRequestResult(boxNumber: String, result: MarkedInfoResult) {
        if (result.status == BOX_MARK_OK) {
            updateProperties.invoke(result.properties.orEmpty())
            handleScannedBox(boxNumber, result.marks.orEmpty())
        } else {
            navigator.openInfoScreen(result.statusDescription.orEmpty())
        }
    }

    companion object {
        private const val COMMON_MODE_MARK_OK = "0"
        private const val SPECIAL_MODE_MARK_OK = "1"
        private const val BOX_MARK_OK = "100"
    }

}