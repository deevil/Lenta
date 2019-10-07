package com.lenta.bp14.features.price_check.good_info

import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.check_price.ICheckPriceTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.analyseCode
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoPcViewModel : CoreViewModel() {
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var task: ICheckPriceTask

    private val priceInfo by lazy {
        task.getProcessingActualPrice()
    }

    val priceInfoUi by lazy {
        ActualPriceInfoUi(
                price1 = priceInfo?.price1,
                price2 = priceInfo?.price2,
                price1Promotion = priceInfo?.price3,
                price2Sale = priceInfo?.price4
        )
    }

    fun getTitle(): String {
        return "${priceInfo?.matNumber?.takeLast(6)} ${priceInfo?.productName}"
    }

    fun onClickNoPrice() {
        setNewCheckStatusAndGoBack(null)
    }

    fun onClickNotValid() {
        setNewCheckStatusAndGoBack(false)
    }

    fun onClickValid() {
        setNewCheckStatusAndGoBack(true)

    }

    fun onScanResult(data: String) {
        checkCode(data)
    }

    private fun checkCode(code: String?) {
        analyseCode(
                code = code ?: "",
                funcForEan = { eanCode ->
                    searchCode(eanCode = eanCode)
                },
                funcForMatNr = { matNr ->
                    searchCode(matNr = matNr)
                },
                funcForPriceQrCode = { qrCode ->
                    searchCode(qrCode = qrCode)
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }

    private fun searchCode(eanCode: String? = null, matNr: String? = null, qrCode: String? = null) {
        viewModelScope.launch {
            require((eanCode != null) xor (matNr != null) xor (qrCode != null)) {
                "only one param allowed. eanCode: $eanCode, matNr: $matNr, qrCode: $qrCode "
            }
            navigator.showProgressLoadingData()

            when {
                !eanCode.isNullOrBlank() -> task.getActualPriceByEan(eanCode)
                !matNr.isNullOrBlank() -> task.getActualPriceByMatNr(matNr)
                !qrCode.isNullOrBlank() -> task.checkPriceByQrCode(qrCode)
                else -> {
                    navigator.showGoodNotFound()
                    return@launch
                }
            }.either(
                    fnL = {
                        navigator.openAlertScreen(it)
                    }
            ) {
                onClickValid()
                if (qrCode.isNullOrBlank()) {
                    task.processingMatNumber = it.matNumber
                    navigator.openGoodInfoPcScreen()
                }
            }
            navigator.hideProgress()

        }
    }

    private fun setNewCheckStatusAndGoBack(isValid: Boolean?) {
        task.setCheckPriceStatus(isValid)
        if (isValid != true) {
            navigator.showPrintPriceOffer(getTitle()) {
                navigator.goBack()
                navigator.openPrintSettingsScreen()
            }
        } else {
            navigator.goBack()
        }

    }

}

data class ActualPriceInfoUi(
        val price1: Float?,
        val price2: Float?,
        val price1Promotion: Float?,
        val price2Sale: Float?
)
