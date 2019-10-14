package com.lenta.bp14.features.work_list.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.check_price.IPriceInfoParser
import com.lenta.bp14.models.data.GoodsListTab
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.models.work_list.Good
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.work_list.WorkListSendReportNetRequest
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.analyseCode
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.sumWith
import com.lenta.shared.utilities.extentions.dropZeros
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListWlViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var task: WorkListTask
    @Inject
    lateinit var priceInfoParser: IPriceInfoParser
    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager
    @Inject
    lateinit var deviceInfo: DeviceInfo
    @Inject
    lateinit var sentReportRequest: WorkListSendReportNetRequest


    val processedSelectionsHelper = SelectionItemsHelper()
    val searchSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)
    val correctedSelectedPage = selectedPage.map { getCorrectedPagePosition(it) }

    val taskName = MutableLiveData("")

    val numberField: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val processingGoods: MutableLiveData<List<ProcessingListUi>> by lazy {
        task.goods.map { list ->
            list?.filter { !it.isProcessed }?.mapIndexed { index, good ->
                ProcessingListUi(
                        position = (index + 1).toString(),
                        material = good.material,
                        name = good.getFormattedMaterialWithName()
                )
            }
        }
    }

    val processedGoods: MutableLiveData<List<ProcessedListUi>> by lazy {
        task.processed.map { list: MutableList<Good>? ->
            list?.mapIndexed { index, good ->
                var total = 0.0
                for (scanResult in good.scanResults.value!!) {
                    total = total.sumWith(scanResult.quantity)
                }

                ProcessedListUi(
                        position = (index + 1).toString(),
                        material = good.material,
                        name = good.getFormattedMaterialWithName(),
                        quantity = total.dropZeros()
                )
            }
        }
    }

    val searchGoods = MutableLiveData<List<Good>>()

    private val selectedItemOnCurrentTab: MutableLiveData<Boolean> = selectedPage
            .combineLatest(processedSelectionsHelper.selectedPositions)
            .map {
                val tab = it?.first?.toInt()
                val processedSelected = it?.second?.isNotEmpty() == true
                tab == GoodsListTab.PROCESSED.position && processedSelected || tab == GoodsListTab.SEARCH.position
            }

    val deleteButtonEnabled = selectedItemOnCurrentTab.map { it }
    val thirdButtonVisibility = selectedPage.map { it != GoodsListTab.PROCESSING.position }

    // -----------------------------

    init {
        viewModelScope.launch {
            task.loadTaskList()
            task.currentGood.value = task.goods.value?.get(0)

            requestFocusToNumberField.value = true
            taskName.value = "${task.getTaskType().taskType} // ${task.getTaskName()}"
        }
    }

    // -----------------------------

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickSave() {
        // Подтверждение - Перевести задание в статус "Подсчитано" и закрыть его для редактирования? - Назад / Да
        navigator.showSetTaskToStatusCalculated {
            viewModelScope.launch {
                navigator.showProgressLoadingData()
                sentReportRequest(task.getReportData(deviceInfo.getDeviceIp())).either(
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

    fun onClickDelete() {

    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkEnteredNumber(numberField.value ?: "")
        return true
    }

    private fun checkEnteredNumber(number: String) {
        analyseCode(
                code = number,
                funcForEan = { eanCode ->
                    addGoodByEan(eanCode)
                },
                funcForMatNr = { matNr ->
                    addGoodByMaterial(matNr)
                },
                funcForPriceQrCode = { qrCode ->
                    priceInfoParser.getPriceInfoFromRawCode(qrCode)?.let {
                        viewModelScope.launch {
                            addGoodByEan(it.eanCode)
                        }
                        return@analyseCode
                    }
                    navigator.showGoodNotFound()
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }

    private fun addGoodByEan(ean: String) {
        Logg.d { "Entered EAN: $ean" }
        viewModelScope.launch {
            /*if (task.addGood(ean)) {
                navigator.openGoodInfoWlScreen()
            }*/
        }
    }

    private fun addGoodByMaterial(material: String) {
        Logg.d { "Entered MATERIAL: $material" }
        viewModelScope.launch {
            navigator.showProgressLoadingData()
            task.getGoodByMaterial(material)?.let { good ->
                task.addGood(good)
                navigator.hideProgress()
                navigator.openGoodInfoWlScreen()
            }
            navigator.hideProgress()
        }
    }

    fun onClickFilter() {
        navigator.openSearchFilterWlScreen()
    }

    fun onClickItemPosition(position: Int) {
        when(getCorrectedPagePosition(selectedPage.value)){
            0 -> processingGoods.value?.get(position)?.material
            1 -> processedGoods.value?.get(position)?.material
            2 -> searchGoods.value?.get(position)?.material
            else -> null
        }?.let { material ->
            viewModelScope.launch {
                navigator.showProgressLoadingData()
                task.getGoodByMaterial(material)?.let { good ->
                    task.addGood(good)
                    navigator.hideProgress()
                    navigator.openGoodInfoWlScreen()
                }
                navigator.hideProgress()
            }
        }
    }

    fun getPagesCount(): Int {
        return if (task.isFreeMode()) 2 else 3
    }

    fun getCorrectedPagePosition(position: Int?): Int {
        return if (getPagesCount() == 3) position ?: 0 else (position ?: 0) + 1
    }

    fun onDigitPressed(digit: Int) {
        numberField.postValue(numberField.value ?: "" + digit)
        requestFocusToNumberField.value = true
    }

    fun onScanResult(data: String) {

    }

}

data class ProcessingListUi(
        val position: String,
        val material: String,
        val name: String
)

data class ProcessedListUi(
        val position: String,
        val material: String,
        val name: String,
        val quantity: String
)
