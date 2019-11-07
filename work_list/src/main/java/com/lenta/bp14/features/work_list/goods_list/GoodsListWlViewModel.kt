package com.lenta.bp14.features.work_list.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.check_price.IPriceInfoParser
import com.lenta.bp14.models.data.GoodsListTab
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.models.work_list.Good
import com.lenta.bp14.models.work_list.IWorkListTask
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
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListWlViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var task: IWorkListTask
    @Inject
    lateinit var priceInfoParser: IPriceInfoParser
    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager
    @Inject
    lateinit var deviceInfo: DeviceInfo
    @Inject
    lateinit var sentReportRequest: WorkListSendReportNetRequest


    val processedSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)
    val correctedSelectedPage = selectedPage.map { getCorrectedPagePosition(it) }

    val taskName = MutableLiveData("")

    val good by lazy { task.currentGood }

    val numberField: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    private val toUiFunc = { products: List<Good>? ->
        products?.mapIndexed { index, good ->
            val total = good.getTotalQuantity()
            ItemWorkListUi(
                    position = (products.size - index).toString(),
                    material = good.material,
                    name = good.getFormattedMaterialWithName(),
                    quantity = "${total.dropZeros()} ${good.units.name}"
            )
        }
    }

    val processingGoods: MutableLiveData<List<ItemWorkListUi>> by lazy {
        task.getProcessingList().map(toUiFunc)
    }

    val processedGoods: MutableLiveData<List<ItemWorkListUi>> by lazy {
        task.getProcessedList().map(toUiFunc)
    }

    val searchGoods: MutableLiveData<List<ItemWorkListUi>> by lazy {
        task.getSearchList().map(toUiFunc)
    }

    val middleButtonEnabled: MutableLiveData<Boolean> = correctedSelectedPage
            .combineLatest(processedSelectionsHelper.selectedPositions)
            .map {
                val tab = it?.first
                val isItemsSelected = it?.second?.isNotEmpty() ?: false

                tab == GoodsListTab.PROCESSED.position && isItemsSelected || tab == GoodsListTab.SEARCH.position
            }

    // -----------------------------

    init {
        viewModelScope.launch {
            task.loadMaxTaskPositions()
            if (!task.isLoadedTaskList) task.loadTaskList()
            if (!task.isEmpty()) task.currentGood.value = task.goods.value?.get(0)

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
        val materials = mutableListOf<String>()
        processedSelectionsHelper.selectedPositions.value?.map { position ->
            processedGoods.value?.get(position)?.material?.let {
                materials.add(it)
            }
        }

        processedSelectionsHelper.clearPositions()
        task.deleteSelectedGoods(materials)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkEnteredNumber(numberField.value ?: "")
        return true
    }

    private fun checkEnteredNumber(number: String) {
        analyseCode(
                code = number,
                funcForEan = { ean ->
                    searchCode(ean = ean)
                },
                funcForMatNr = { material ->
                    searchCode(material = material)
                },
                funcForPriceQrCode = { qrCode ->
                    priceInfoParser.getPriceInfoFromRawCode(qrCode)?.let {
                        searchCode(ean = it.eanCode)
                        return@analyseCode
                    }
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }

    private fun searchCode(ean: String? = null, material: String? = null) {
        viewModelScope.launch {
            require((ean != null) xor (material != null)) {
                "Only one param allowed - ean: $ean, material: $material"
            }

            navigator.showProgressLoadingData()

            when {
                !ean.isNullOrBlank() -> task.getGoodByEan(ean)
                !material.isNullOrBlank() -> task.getGoodByMaterial(material)
                else -> null
            }.also {
                navigator.hideProgress()
            }?.let { good ->
                if (task.getDescription().isStrictList && !task.isGoodFromTask(good)) {
                    navigator.showGoodIsNotPartOfTask()
                } else {
                    task.addGoodToList(good)
                    navigator.openGoodInfoWlScreen()
                }
                return@launch
            }

            navigator.showGoodNotFound()
        }
    }

    fun onClickFilter() {
        navigator.openSearchFilterWlScreen()
    }

    fun onClickItemPosition(position: Int) {
        when (correctedSelectedPage.value) {
            0 -> processingGoods.value?.get(position)?.material
            1 -> processedGoods.value?.get(position)?.material
            2 -> searchGoods.value?.get(position)?.material
            else -> null
        }?.let { material ->
            searchCode(material = material)
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
        checkEnteredNumber(data)
    }

    fun updateGoodList() {
        task.updateGoodList()
    }

}

data class ItemWorkListUi(
        val position: String,
        val material: String,
        val name: String,
        val quantity: String = ""
)
