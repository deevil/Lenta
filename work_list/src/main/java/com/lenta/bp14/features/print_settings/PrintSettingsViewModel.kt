package com.lenta.bp14.features.print_settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.check_price.IPriceInfoParser
import com.lenta.bp14.models.print.IPrintTask
import com.lenta.bp14.models.print.PriceTagType
import com.lenta.bp14.models.print.PrinterType
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.analyseCode
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toNullIfEmpty
import com.lenta.shared.utilities.extentions.toSapBooleanString
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class PrintSettingsViewModel : CoreViewModel(), OnPositionClickListener, OnOkInSoftKeyboardListener {


    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var productInfoNetRequest: ProductInfoNetRequest

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var printTask: IPrintTask

    @Inject
    lateinit var priceInfoParser: IPriceInfoParser

    private var productInfoResult: MutableLiveData<ProductInfoResult?> = MutableLiveData(null)

    val numberField = MutableLiveData("")

    private val printerTypes by lazy {
        MutableLiveData<List<PrinterType>>().also {
            viewModelScope.launch {
                it.value = printTask.getPrinterTypes()
            }
        }
    }

    private val printerPriceTypes by lazy {
        MutableLiveData<List<PriceTagType>>().also {
            viewModelScope.launch {
                it.value = printTask.getPriceTagTypes()

            }
        }
    }


    val title = productInfoResult.map {
        it?.productsInfo?.getOrNull(0)?.let {
            "${it.matNr.takeLast(6)} ${it.name}"
        } ?: ""
    }

    val printerTypesTitles = printerTypes.map { list -> list?.map { it.name } }
    val printerPriceTypesTitles = printerPriceTypes.map { list -> list?.map { it.name } }

    val numberOfCopies: MutableLiveData<String> = MutableLiveData("1")
    val selectedPrinterTypePos: MutableLiveData<Int> = MutableLiveData(0)
    val selectedPriceTagTypePos: MutableLiveData<Int> = MutableLiveData(0)
    val ipAddressVisibility: MutableLiveData<Boolean> = selectedPrinterTypePos.map {
        getSelectedPrinterType()?.isMobile == true
    }


    private fun getSelectedPrinterType(): PrinterType? {
        return printerTypes.value?.getOrNull(selectedPrinterTypePos.value ?: 0)
    }

    private fun getSelectedPriceType(): PriceTagType? {
        return printerPriceTypes.value?.getOrNull(selectedPriceTagTypePos.value ?: 0)
    }


    val ipAddress: MutableLiveData<String> = MutableLiveData("")

    val isPrintEnabled = numberOfCopies
            .combineLatest(selectedPrinterTypePos)
            .combineLatest(selectedPriceTagTypePos)
            .combineLatest(ipAddress)
            .combineLatest(title)
            .map {
                if (numberOfCopies.value?.toIntOrNull() ?: 0 < 1) {
                    return@map false
                }

                if (selectedPrinterTypePos.value ?: 0 < 1) {
                    return@map false
                }

                if (selectedPriceTagTypePos.value ?: 0 < 1) {
                    return@map false
                }

                if (ipAddressVisibility.value == true && ipAddress.value.isNullOrBlank()) {
                    return@map false
                }

                if (productInfoResult.value == null) {
                    return@map false
                }

                return@map true
            }

    val onTypeTagPositionClick = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedPriceTagTypePos.value = position
        }
    }

    fun increaseNumberOfCopies() {
        val copy = numberOfCopies.value?.toIntOrNull() ?: 0
        numberOfCopies.value = "" + (copy + 1)
    }

    fun reduceNumberOfCopies() {
        val copy = numberOfCopies.value?.toIntOrNull() ?: 0
        if (copy > 1) numberOfCopies.value = "" + (copy - 1)
    }

    override fun onClickPosition(position: Int) {
        selectedPrinterTypePos.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkCode(numberField.value)
        return true
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
                    priceInfoParser.getPriceInfoFromRawCode(qrCode)?.let {
                        searchCode(eanCode = it.eanCode)
                        return@analyseCode
                    }
                    navigator.showGoodNotFound()
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }


    private fun searchCode(eanCode: String? = null, matNr: String? = null) {
        require((!eanCode.isNullOrBlank() xor !matNr.isNullOrBlank()))
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            productInfoNetRequest(
                    ProductInfoParams(
                            withProductInfo = true.toSapBooleanString(),
                            withAdditionalInf = true.toSapBooleanString(),
                            tkNumber = sessionInfo.market!!,
                            taskType = "ПЦН",
                            eanList = if (eanCode != null) listOf(EanParam(eanCode)) else null,
                            matNrList = if (matNr != null) listOf(MatNrParam(matNr)) else null
                    )
            ).either({
                productInfoResult.value = null
                navigator.openAlertScreen(it)
            }) {
                Logg.d { "productInfoResult: $it" }
                productInfoResult.value = it

                it.prices.getOrNull(0)?.let { priceInfo ->
                    setLabel(isRegular = priceInfo.price3.toNullIfEmpty() == null && priceInfo.price4.toNullIfEmpty() == null)
                }

                true
            }

            navigator.hideProgress()
        }


    }

    private fun setLabel(isRegular: Boolean) {
        printerPriceTypes
                .value?.indexOfFirst { it.isRegular == isRegular }?.let { pos ->
            if (pos >= 0) {
                selectedPriceTagTypePos.postValue(pos)
            }
        }
    }

    fun onClickPrint() {
        viewModelScope.launch {

            navigator.showProgressLoadingData()
            printTask.printPrice(
                    ip = ipAddress.value ?: "",
                    productInfoResult = productInfoResult.value!!,
                    printerType = getSelectedPrinterType()!!,
                    isRegular = getSelectedPriceType()!!.isRegular?: false,
                    copies = numberOfCopies.value?.toIntOrNull() ?: 0
            ).either({
                navigator.openAlertScreen(it)
            }) {
                navigator.showPriceTagsSubmitted()
            }

            navigator.hideProgress()

        }
    }
}

