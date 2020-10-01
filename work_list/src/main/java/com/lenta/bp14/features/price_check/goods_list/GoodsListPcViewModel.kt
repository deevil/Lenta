package com.lenta.bp14.features.price_check.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.features.common_ui_model.SimpleProductUi
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.check_price.ActualPriceInfo
import com.lenta.bp14.models.check_price.CheckPriceResult
import com.lenta.bp14.models.check_price.ICheckPriceTask
import com.lenta.bp14.models.data.GoodsListTab
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.models.print.IPrintTask
import com.lenta.bp14.models.print.PriceTagType
import com.lenta.bp14.models.print.PrintInfo
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.check_price.CheckPriceReportNetRequest
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.actionByNumber
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GoodsListPcViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener, OnPositionClickListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var task: ICheckPriceTask

    @Inject
    lateinit var checkPriceReportNetRequest: CheckPriceReportNetRequest

    @Inject
    lateinit var deviceInfo: DeviceInfo

    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager

    @Inject
    lateinit var printTask: IPrintTask


    private val tagTypes by lazy {
        mutableListOf<PriceTagType>()
    }

    val tagTypeTitles by lazy {
        MutableLiveData<List<String>>(
                emptyList()
        ).also { liveData ->
            launchUITryCatch {
                tagTypes.clear()
                liveData.value = printTask.getPriceTagTypes().map {
                    tagTypes.add(it)
                    it.name
                }
            }
        }
    }

    val tagTypesPosition = MutableLiveData(0)

    val processedSelectionsHelper = SelectionItemsHelper()
    val searchSelectionsHelper = SelectionItemsHelper()

    val correctedSelectedPage = selectedPage.map { getCorrectedPagePosition(it) }

    val taskName by lazy {
        "${task.getTaskType().taskType} // ${task.getTaskName()}"
    }

    val numberField = MutableLiveData<String>("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val processingGoods by lazy {
        task.getToProcessingProducts().map { list ->
            list?.mapIndexed { index, iCheckPriceResult ->
                SimpleProductUi(
                        position = list.size - index,
                        matNr = iCheckPriceResult.matNr.orEmpty(),
                        name = "${iCheckPriceResult.matNr?.takeLast(6)} ${iCheckPriceResult.name}"
                )
            }
        }
    }

    private val funcUiAdapter = { list: List<CheckPriceResult>? ->
        list?.reversed()?.mapIndexed { index, iCheckPriceResult ->
            val isAllValid = iCheckPriceResult.isAllValid()
            CheckPriceResultUi(
                    matNr = iCheckPriceResult.matNr!!,
                    position = list.size - index,
                    name = "${iCheckPriceResult.matNr.takeLast(6)} ${iCheckPriceResult.name}",
                    isPriceValid = isAllValid,
                    isPrinted = if (isAllValid == true) null else iCheckPriceResult.isPrinted
            )
        }
    }

    val processedGoods by lazy {
        task.getCheckResults().map(funcUiAdapter)
    }

    private val searchCheckResults by lazy {
        task.getCheckResultsForPrint(tagTypesPosition.map {
            tagTypes.getOrNull(it ?: -1)
        })
    }

    val searchGoods by lazy {
        searchCheckResults.map(funcUiAdapter)
    }

    private val selectedItemOnCurrentTab: MutableLiveData<Boolean> = correctedSelectedPage
            .combineLatest(processedSelectionsHelper.selectedPositions)
            .combineLatest(searchSelectionsHelper.selectedPositions)
            .map {
                val tab = it?.first?.first?.toInt()
                val processedSelected = it?.first?.second?.isNotEmpty() == true
                val searchSelected = it?.second?.isNotEmpty() == true
                tab == GoodsListTab.PROCESSED.position && processedSelected || tab == GoodsListTab.SEARCH.position && searchSelected
            }

    val deleteButtonEnabled = selectedItemOnCurrentTab.map { it }
    val printButtonEnabled = tagTypesPosition.map { it ?: 0 > 0 }
    val videoButtonEnabled by lazy {
        MutableLiveData(task.isFreeMode())
    }

    val saveButtonEnabled by lazy {
        processedGoods.map { it?.isNotEmpty() ?: false }
    }

    val videoButtonVisibility = correctedSelectedPage.map { it != GoodsListTab.SEARCH.position }
    val deleteButtonVisibility = correctedSelectedPage.map { it != GoodsListTab.PROCESSING.position }
    val printButtonVisibility = correctedSelectedPage.map { it != GoodsListTab.PROCESSED.position }

    // -----------------------------

    init {
        launchUITryCatch {
            requestFocusToNumberField.value = true
        }
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkCode(numberField.value)
        return true
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
                    task.processingMatNumber = it.matNumber
                    navigator.openGoodInfoPcScreen()
                } else if (it is CheckPriceResult) {
                    if (it.isPriceValid() != true) {
                        task.processingMatNumber = it.matNr
                        navigator.openGoodInfoPcScreen()
                    }
                }
            }

            navigator.hideProgress()
        }
    }

    fun onClickSave() {
        if (task.isHaveDiscrepancies()) {
            navigator.openListOfDifferencesScreen(
                    onClickSkipCallback = {
                        showConfirmationForSentReportScreen()
                    }
            )
        } else {
            showConfirmationForSentReportScreen()
        }
    }

    private fun showConfirmationForSentReportScreen() {
        navigator.showSetTaskToStatusCalculated {
            launchUITryCatch {
                navigator.showProgressLoadingData(::handleFailure)
                checkPriceReportNetRequest(
                        task.getReportData(
                                ip = deviceInfo.getDeviceIp()
                        )
                ).either({
                    navigator.openAlertScreen(it)
                }) {
                    Logg.d { "SentReportResult: $it" }
                    generalTaskManager.clearCurrentTask(sentReportResult = it)
                    navigator.openReportResultScreen()
                }

                navigator.hideProgress()
            }
        }
    }

    fun onClickDelete() {
        when (correctedSelectedPage.value) {
            1 -> processedSelectionsHelper
            2 -> searchSelectionsHelper
            else -> null
        }?.let { selectionHelper ->
            selectionHelper.selectedPositions.value?.apply {
                task.removeCheckResultsByMatNumbers(
                        if (selectionHelper === processedSelectionsHelper) {
                            processedGoods
                        } else {
                            searchGoods
                        }.value?.filterIndexed { index, _ ->
                            this.contains(index)
                        }?.map { it.matNr }?.toSet()
                                ?: emptySet()
                )
            }

            selectionHelper.clearPositions()
        }
    }

    fun onClickPrint() {
        launchUITryCatch {
            var printerName = ""
            withContext(IO) {
                printerName = printTask.getPrinterTypes().firstOrNull { it.isStatic == true }?.name
                       .orEmpty()
            }

            getResultsForPrint().let { printList ->
                if (printList.size == 1) {
                    printTask.matNrForPrint = printList[0].matNr
                    navigator.openPrintSettingsScreen()
                } else {
                    if (selectedPriceTagIsRed()) {
                        navigator.showMakeSureRedPaperInstalled(printerName, numberOfCopy = 1) {
                            print()
                        }
                    } else {
                        navigator.showMakeSureYellowPaperInstalled(printerName, numberOfCopy = 1) {
                            print()
                        }
                    }
                }
            }
        }
    }

    private fun selectedPriceTagIsRed(): Boolean {
        return tagTypes.getOrNull(tagTypesPosition.value ?: -1)?.isRegular == false
    }

    private fun print() {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)
            val selectedSearchTasks = getResultsForPrint()
            val printTasks = getPrintTasks()
            printTask.printToBigDataMax(printTasks).either({
                navigator.openAlertScreen(failure = it)

            }) {
                task.markPrinted(selectedSearchTasks.map { it.matNr!! })
                navigator.showPriceTagsSubmitted {
                    //ничего не делаем
                }
            }
            navigator.hideProgress()
        }
    }

    private fun getPrintTasks(): List<PrintInfo> {
        return getResultsForPrint().map {
            PrintInfo(
                    barCode = it.ean,
                    amount = 1,
                    templateCode = if (tagTypes.getOrNull(tagTypesPosition.value
                                    ?: -1)?.isRegular == true) 1 else 2
            )
        }
    }

    private fun getResultsForPrint(): List<CheckPriceResult> {
        val searchList = searchCheckResults.value!!
        if (searchSelectionsHelper.isSelectedEmpty()) {
            return searchList
        }
        return searchSelectionsHelper.selectedPositions.value!!.map {
            searchList.get(it)
        }
    }

    fun onClickVideo() {
        navigator.openScanPriceScreen()
    }

    fun onClickItemPosition(position: Int) {
        getMatNrByPosition(position)?.let { selectedMatNr ->
            task.processingMatNumber = selectedMatNr
            navigator.openGoodInfoPcScreen()
        }
    }

    private fun getMatNrByPosition(position: Int): String? {
        return when (correctedSelectedPage.value) {
            0 -> processingGoods.value?.map { it.matNr }
            1 -> processedGoods.value?.map { it.matNr }
            2 -> searchGoods.value?.map { it.matNr }
            else -> null
        }.let {
            it?.getOrNull(position)
        }
    }

    fun getPagesCount(): Int {
        return if (task.isFreeMode()) 2 else 3
    }

    fun getCorrectedPagePosition(position: Int?): Int {
        return if (getPagesCount() == 3) position ?: 0 else (position ?: 0) + 1
    }

    fun onScanResult(data: String) {
        checkCode(data)
    }

    override fun onClickPosition(position: Int) {
        tagTypesPosition.value = position
    }

    fun showVideoErrorMessage() {
        navigator.showDeviceNotSupportVideoScan()
    }

}


data class CheckPriceResultUi(
        val matNr: String,
        val position: Int,
        val name: String,
        val isPriceValid: Boolean?,
        val isPrinted: Boolean?
)