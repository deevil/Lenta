package com.lenta.bp14.features.print_settings

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.check_price.ICheckPriceTask
import com.lenta.bp14.models.check_price.IPriceInfoParser
import com.lenta.bp14.models.print.IPrintTask
import com.lenta.bp14.models.print.PriceTagType
import com.lenta.bp14.models.print.PrinterType
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.actionByNumber
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class PrintSettingsViewModel : CoreViewModel(), OnPositionClickListener, OnOkInSoftKeyboardListener {


    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var productInfoNetRequest: ProductInfoNetRequest

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var task: IPrintTask

    @Inject
    lateinit var priceInfoParser: IPriceInfoParser

    @Inject
    lateinit var printSettings: PrintSettings

    @Inject
    lateinit var generalManager: IGeneralTaskManager


    private var productInfoResult: MutableLiveData<ProductInfoResult?> = MutableLiveData(null)

    val numberField = MutableLiveData("")

    private val printerTypes by lazy {
        MutableLiveData<List<PrinterType>>().also {
            launchUITryCatch {
                it.value = task.getPrinterTypes()
            }
        }
    }

    private val printerPriceTypes by lazy {
        MutableLiveData<List<PriceTagType>>().also {
            launchUITryCatch {
                it.value = task.getPriceTagTypes()

            }
        }
    }


    val title = productInfoResult.map {
        it?.productsInfo?.getOrNull(0)?.let {
            "${it.matNr.takeLast(6)} ${it.name}"
        }.orEmpty()
    }

    val printerTypesTitles = printerTypes.map { list -> list?.map { it.name } }
    val printerPriceTypesTitles = printerPriceTypes.map { list -> list?.map { it.name } }

    val numberOfCopies: MutableLiveData<String> = MutableLiveData("1")
    val selectedPrinterTypePos: MutableLiveData<Int> = MutableLiveData(0)
    val selectedPriceTagTypePos: MutableLiveData<Int> = MutableLiveData(0)
    val ipAddressVisibility: MutableLiveData<Boolean> = selectedPrinterTypePos.combineLatest(printerTypes).map {
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

    private var needGoBackAfterPrint = false

    // -----------------------------

    init {
        launchUITryCatch {
            task.loadMaxPrintCopy()
            task.matNrForPrint?.let {
                numberField.value
                checkCode(it)
                needGoBackAfterPrint = true
                task.matNrForPrint = null
            }
        }
    }

    // -----------------------------

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

    fun onClickPrint() {
        val copies = numberOfCopies.value?.toIntOrNull() ?: 0

        Logg.d { "--> Max allowed copies: ${task.getMaxCopies()} / Current copies: $copies" }
        if (copies > task.getMaxCopies()) {
            navigator.showNumberOfCopiesExceedsMaximum()
            return
        }

        if (selectedPrinterIsBigDatamax()) {
            if (selectedPriceTagIsRed()) {
                navigator.showMakeSureRedPaperInstalled(getSelectedPrinterType()!!.name, copies) {
                    print()
                }
            } else {
                navigator.showMakeSureYellowPaperInstalled(getSelectedPrinterType()!!.name, copies) {
                    print()
                }
            }
        } else if (copies > 1) {
            navigator.showConfirmPriceTagsPrinting(copies) {
                print()
            }
        } else {
            print()
        }
    }

    fun restoreSettings() {
        printSettings.printerIp?.let {
            ipAddress.value = it
        }

        printSettings.printerTypePos.let {
            if (it > -1) {
                selectedPrinterTypePos.value = it
            }
        }

        printSettings.priceTypePos.let {
            if (it > -1) {
                selectedPriceTagTypePos.value = it
            }
        }

    }

    fun saveSettings() {
        printSettings.printerIp = ipAddress.value
        printSettings.printerTypePos = selectedPrinterTypePos.value ?: 0
        printSettings.priceTypePos = selectedPriceTagTypePos.value ?: 0
    }

    private fun checkCode(code: String?) {
        actionByNumber(
                number = code.orEmpty(),
                funcForEan = { ean, _ -> searchCode(ean) },
                funcForMaterial = { material -> searchCode(material) },
                funcForPriceQrCode = { qrCode ->
                    priceInfoParser.getPriceInfoFromRawCode(qrCode)?.let {
                        searchCode(it.eanCode)
                    } ?: navigator.showGoodNotFound()
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }


    private fun searchCode(eanCode: String? = null, matNr: String? = null) {
        require((!eanCode.isNullOrBlank() xor !matNr.isNullOrBlank()))
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)

            productInfoNetRequest(
                    ProductInfoParams(
                            withProductInfo = true.toSapBooleanString(),
                            withAdditionalInf = true.toSapBooleanString(),
                            tkNumber = sessionInfo.market.orEmpty(),
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


    private fun print() {
        launchUITryCatch {


            navigator.showProgressConnection()
            task.printPrice(
                    ip = ipAddress.value.orEmpty(),
                    productInfoResult = productInfoResult.value!!,
                    printerType = getSelectedPrinterType()!!,
                    isRegular = getSelectedPriceType()!!.isRegular ?: false,
                    copies = numberOfCopies.value?.toIntOrNull() ?: 0
            ).either({
                navigator.openAlertScreen(it)
                setPrintedProductForCheckPriceTask()

            }) {
                setPrintedProductForCheckPriceTask()
                navigator.showPriceTagsSubmitted {
                    if (needGoBackAfterPrint) {
                        navigator.goBack()
                    }
                }
            }

            navigator.hideProgress()

        }

    }

    private fun setPrintedProductForCheckPriceTask() {
        generalManager.getProcessedTask()?.let { iTask ->
            productInfoResult.value?.productsInfo?.getOrNull(0)?.let {
                if (iTask is ICheckPriceTask) {
                    iTask.markPrinted(listOf(it.matNr))
                }
            }

        }

    }

    private fun selectedPriceTagIsRed(): Boolean {
        return getSelectedPriceType()?.isRegular == false
    }

    private fun selectedPrinterIsBigDatamax(): Boolean {
        return getSelectedPrinterType()?.isStatic == true
    }


}


class PrintSettings @Inject constructor(private val sharedPreferences: SharedPreferences) {

    var printerTypePos: Int
        get() = sharedPreferences.getInt(KEY_PRINTER_TYPE, 0)
        set(value) {
            sharedPreferences.edit().putInt(KEY_PRINTER_TYPE, value).apply()
        }

    var priceTypePos: Int
        get() = sharedPreferences.getInt(KEY_PRICE_TYPE, 0)
        set(value) {
            sharedPreferences.edit().putInt(KEY_PRICE_TYPE, value).apply()
        }

    var printerIp: String?
        get() = sharedPreferences.getString(KEY_PRINTER_IP, null)
        set(value) {
            sharedPreferences.edit().putString(KEY_PRINTER_IP, value).apply()
        }

    companion object {
        val KEY_PRINTER_TYPE = "KEY_PRINTER_TYPE"
        val KEY_PRICE_TYPE = "KEY_PRICE_TYPE"
        val KEY_PRINTER_IP = "KEY_PRINTER_IP"
    }

}

