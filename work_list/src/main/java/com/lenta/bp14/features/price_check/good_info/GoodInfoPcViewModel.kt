package com.lenta.bp14.features.price_check.good_info

import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.check_price.ActualPriceInfo
import com.lenta.bp14.models.check_price.CheckPriceResult
import com.lenta.bp14.models.check_price.ICheckPriceTask
import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.print.IPrintTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.actionByNumber
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoPcViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var task: ICheckPriceTask

    @Inject
    lateinit var printTask: IPrintTask


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

    val options by lazy {
        val options = priceInfo!!.options
        OptionsUi(
                matrixType = options.matrixType,
                goodType = options.goodType,
                section = options.section,
                healthFood = options.healthFood,
                novelty = options.novelty
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
        actionByNumber(
                number = code.orEmpty(),
                funcForEan = { ean, _ -> searchCode(eanCode = ean) },
                funcForMaterial = { material -> searchCode(matNr = material) },
                funcForPriceQrCode = { qrCode -> searchCode(qrCode = qrCode) },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }

    private fun searchCode(eanCode: String? = null, matNr: String? = null, qrCode: String? = null) {
        viewModelScope.launch {
            require((eanCode != null) xor (matNr != null) xor (qrCode != null)) {
                "only one param allowed. eanCode: $eanCode, matNr: $matNr, qrCode: $qrCode "
            }
            navigator.showProgressLoadingData(::handleFailure)

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
                if (it is ActualPriceInfo) {
                    onClickValid()
                    task.processingMatNumber = it.matNumber
                    navigator.openGoodInfoPcScreen()
                } else if (it is CheckPriceResult) {
                    if (it.isPriceValid() != true) {
                        onClickValid()
                        task.processingMatNumber = it.matNr
                        navigator.openGoodInfoPcScreen()
                    }
                }
            }

            navigator.hideProgress()
        }
    }

    private fun setNewCheckStatusAndGoBack(isValid: Boolean?) {
        task.setCheckPriceStatus(isValid)
        if (isValid != true) {
            navigator.showPrintPriceOffer(
                    goodName = getTitle(),
                    noCallback = {
                        navigator.goBack()
                    },
                    yesCallback = {
                        navigator.goBack()
                        printTask.matNrForPrint = task.processingMatNumber
                        navigator.openPrintSettingsScreen()
                    }
            )
        } else {
            navigator.goBack()
        }
    }

}


data class ActualPriceInfoUi(
        val price1: Double?,
        val price2: Double?,
        val price1Promotion: Double?,
        val price2Sale: Double?
)

data class OptionsUi(
        val matrixType: MatrixType,
        val goodType: GoodType,
        val section: String,
        val healthFood: Boolean,
        val novelty: Boolean
)