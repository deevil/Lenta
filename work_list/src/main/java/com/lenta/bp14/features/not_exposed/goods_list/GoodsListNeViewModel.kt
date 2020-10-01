package com.lenta.bp14.features.not_exposed.goods_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.features.common_ui_model.SimpleProductUi
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.check_price.IPriceInfoParser
import com.lenta.bp14.models.data.GoodsListTab
import com.lenta.bp14.models.filter.FilterFieldType
import com.lenta.bp14.models.filter.FilterParameter
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.models.not_exposed.INotExposedTask
import com.lenta.bp14.models.not_exposed.repo.NotExposedProductInfo
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.not_exposed_product.NotExposedSendReportNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequest
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequestParams
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.actionByNumber
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListNeViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var task: INotExposedTask

    @Inject
    lateinit var deviceInfo: DeviceInfo

    @Inject
    lateinit var sentReportRequest: NotExposedSendReportNetRequest

    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager

    @Inject
    lateinit var priceInfoParser: IPriceInfoParser

    @Inject
    lateinit var scanInfoRequest: ScanInfoRequest

    @Inject
    lateinit var sessionInfo: ISessionInfo


    val onOkFilterListener = object : OnOkInSoftKeyboardListener {
        override fun onOkInSoftKeyboard(): Boolean {
            applyFilter()
            return true
        }
    }

    val processedSelectionsHelper = SelectionItemsHelper()

    val correctedSelectedPage = selectedPage.map { getCorrectedPagePosition(it) }

    val taskName by lazy {
        "${task.getTaskType().taskType} // ${task.getTaskName()}"
    }

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val filterField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val processingGoods by lazy {
        task.getToProcessingProducts().map { list ->
            list?.mapIndexed { index, productInfo ->
                SimpleProductUi(
                        position = list.size - index,
                        matNr = productInfo.matNr,
                        name = "${productInfo.matNr.takeLast(6)} ${productInfo.name}"
                )
            }
        }
    }

    private val toUiFunc = { products: List<NotExposedProductInfo>? ->
        products?.reversed()?.mapIndexed { index, productInfo ->
            NotExposedProductUi(
                    position = products.size - index,
                    matNr = productInfo.matNr,
                    name = "${productInfo.matNr.takeLast(6)} ${productInfo.name}",
                    quantity = "${productInfo.quantity.toStringFormatted()} ${productInfo.units?.name
                           .orEmpty()}",
                    isEmptyPlaceMarked = productInfo.isEmptyPlaceMarked
            )
        }
    }

    val processedGoods: LiveData<List<NotExposedProductUi>> by lazy {
        task.getProducts().map(toUiFunc)
    }
    val searchGoods: LiveData<List<NotExposedProductUi>> by lazy {
        task.getFilteredProducts().map(toUiFunc)
    }

    private val selectedItemOnCurrentTab: MutableLiveData<Boolean> = correctedSelectedPage
            .combineLatest(processedSelectionsHelper.selectedPositions)
            .map {
                val tab = it?.first?.toInt()
                val processedSelected = it?.second?.isNotEmpty() == true
                tab == GoodsListTab.PROCESSED.position && processedSelected || tab == GoodsListTab.SEARCH.position
            }

    val saveButtonEnabled by lazy { processedGoods.map { it?.isNotEmpty() ?: false } }

    val thirdButtonEnabled by lazy {
        selectedItemOnCurrentTab.combineLatest(saveButtonEnabled).map {
            when (correctedSelectedPage.value) {
                0, 1 -> selectedItemOnCurrentTab.value
                2 -> saveButtonEnabled.value
                else -> null
            }
        }
    }

    val thirdButtonVisibility = correctedSelectedPage.map { it != GoodsListTab.PROCESSING.position }

    init {
        launchUITryCatch {
            requestFocusToNumberField.value = true
        }
    }

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
                funcForPriceQrCode = { qrCode ->
                    priceInfoParser.getPriceInfoFromRawCode(qrCode)?.let {
                        searchCode(eanCode = it.eanCode)
                    } ?: navigator.showGoodNotFound()
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }


    private fun searchCode(eanCode: String? = null, matNr: String? = null) {
        require((!eanCode.isNullOrBlank() xor !matNr.isNullOrBlank()))
        viewModelScope.launch {
            navigator.showProgressLoadingData(::handleFailure)

            var scanInfoResult: ScanInfoResult? = null
            var quantity = 0.0

            if (eanCode != null) {
                var failureScanInfoRequest: Failure? = null
                val scanCodeInfo = ScanCodeInfo(eanCode)

                scanInfoRequest(
                        ScanInfoRequestParams(
                                number = eanCode,
                                fromScan = true,
                                isBarCode = true,
                                tkNumber = sessionInfo.market!!
                        )
                ).also {
                    navigator.hideProgress()
                }.either(
                        fnL = {
                            failureScanInfoRequest = it
                            true
                        }
                ) {
                    scanInfoResult = it
                    quantity = scanCodeInfo.getQuantity(units = it.productInfo.uom)
                    true
                }

                failureScanInfoRequest?.let {
                    navigator.openAlertScreen(it)
                    return@launch
                }
            }

            task.getProductInfoAndSetProcessed(
                    matNr = scanInfoResult?.productInfo?.materialNumber
                            ?: matNr,
                    quantity = quantity
            ).also {
                navigator.hideProgress()
            }.either(
                    {
                        navigator.openAlertScreen(failure = it)
                    }
            ) {
                navigator.openGoodInfoNeScreen()
            }
        }
    }

    fun onClickSave() {
        if (task.isHaveDiscrepancies()) {
            navigator.openListOfDifferencesScreen {
                showConfirmationForSave()
            }
        } else {
            showConfirmationForSave()
        }
    }

    private fun showConfirmationForSave() {
        navigator.showSetTaskToStatusCalculated {
            launchUITryCatch {
                navigator.showProgressLoadingData(::handleFailure)
                sentReportRequest(task.getReportData(
                        ip = deviceInfo.getDeviceIp()
                )).either(
                        {
                            navigator.openAlertScreen(failure = it)
                        }
                ) {
                    Logg.d { "SentReportResult: $it" }
                    generalTaskManager.clearCurrentTask(sentReportResult = it)
                    navigator.openReportResultScreen()
                }
                navigator.hideProgress()
            }
        }
    }

    private fun onClickDelete() {
        processedGoods.value!!.filterIndexed { index, _ ->
            processedSelectionsHelper.isSelected(position = index)
        }.map { it.matNr }.toSet().apply {
            task.removeCheckResultsByMatNumbers(this)
        }
        processedSelectionsHelper.clearPositions()
    }

    fun onClickThirdButton() {
        when (correctedSelectedPage.value) {
            1 -> onClickDelete()
            2 -> navigator.openSearchFilterWlScreen()
        }

    }

    fun onClickItemPosition(position: Int) {
        when (correctedSelectedPage.value) {
            0 -> {
                processingGoods.value?.getOrNull(position)?.let {
                    checkCode(it.matNr)
                }
            }
            1 -> {
                (processedGoods.value)?.getOrNull(position)?.let {
                    checkCode(it.matNr)
                }
            }
            2 -> {
                (searchGoods.value)?.getOrNull(position)?.let {
                    checkCode(it.matNr)
                }
            }
        }
    }

    fun getPagesCount(): Int {
        return if (task.isFreeMode()) 2 else 3
    }

    fun getCorrectedPagePosition(position: Int?): Int {
        return if (getPagesCount() == 3) position ?: 0 else (position ?: 0) + 1
    }

    fun applyFilter() {
        task.onFilterChanged(FilterParameter(FilterFieldType.NUMBER, filterField.value
               .orEmpty()))
    }

    fun onScanResult(data: String) {
        checkCode(data)
    }

}


data class NotExposedProductUi(
        val position: Int,
        val matNr: String,
        val name: String,
        val quantity: String?,
        val isEmptyPlaceMarked: Boolean?
)

