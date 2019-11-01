package com.lenta.bp14.features.check_list.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.check_list.Good
import com.lenta.bp14.models.check_list.ICheckListTask
import com.lenta.bp14.models.check_price.IPriceInfoParser
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.check_list.CheckListSendReportNetRequest
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.analyseCode
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListClViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var task: ICheckListTask
    @Inject
    lateinit var priceInfoParser: IPriceInfoParser
    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager
    @Inject
    lateinit var deviceInfo: DeviceInfo
    @Inject
    lateinit var sentReportRequest: CheckListSendReportNetRequest


    val selectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val taskName = MutableLiveData("")

    val numberField: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val goods: MutableLiveData<List<ItemGoodUi>> by lazy {
        task.goods.map { list: List<Good>? ->
            list?.mapIndexed { index, good ->
                ItemGoodUi(
                        position = (index + 1).toString(),
                        name = good.getFormattedMaterialWithName(),
                        quantity = good.quantity,
                        units = good.units
                )
            }
        }
    }

    val deleteButtonEnabled by lazy {
        selectionsHelper.selectedPositions.map { it?.isNotEmpty() ?: false }
    }

    val saveButtonEnabled by lazy {
        goods.map { it?.isNotEmpty() ?: false }
    }

    // -----------------------------

    init {
        viewModelScope.launch {
            task.loadMaxTaskPositions()
            requestFocusToNumberField.value = true
            taskName.value = "${task.getTaskType().taskType} // ${task.getTaskName()}"
        }
    }

    // -----------------------------

    fun onClickDelete() {
        task.deleteSelectedGoods(selectionsHelper.selectedPositions.value!!)
        selectionsHelper.clearPositions()
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

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
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
                numberField.value = ""
                navigator.hideProgress()
            }?.let { good ->
                Logg.d { "--> Max task positions: ${task.getMaxTaskPositions()}" }
                if (task.isReachLimitPositions(good.material)) {
                    navigator.showMaxCountProductAlert()
                    return@launch
                }

                task.addGood(good)
                return@launch
            }

            navigator.showGoodNotFound()
        }
    }

    fun onDigitPressed(digit: Int) {
        numberField.postValue(numberField.value ?: "" + digit)
        requestFocusToNumberField.value = true
    }

    fun onClickVideo() {
        navigator.openVideoScanProductScreen()

    }

    fun onScanResult(data: String) {
        checkEnteredNumber(data)
    }

    fun showVideoErrorMessage() {
        navigator.showDeviceNotSupportVideoScan()
    }

}

data class ItemGoodUi(
        val position: String,
        val name: String,
        val quantity: MutableLiveData<String>,
        val units: Uom
)